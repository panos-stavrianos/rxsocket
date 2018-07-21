package gr.osnet.rxsocket

import android.os.Environment
import android.util.Base64
import com.google.common.io.ByteStreams
import mu.KotlinLogging
import java.io.*
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8


/**
 * Created by panos on 15/11/2017.
 */
private val logger = KotlinLogging.logger {}

object AES {

    fun testingStuff() {
        logger.info { "testingStuff" }
        Thread {
            encrypt(Environment.getDataDirectory().absolutePath + "/TimePatrol/1.jpg", "1234")

            val file = File("${Environment.DIRECTORY_DCIM}/2.jpg")
            val size = file.length()
            val bytes = ByteArray(size.toInt())
            try {
                val buf = BufferedInputStream(FileInputStream(file))
                buf.read(bytes, 0, bytes.size)
                buf.close()
            } catch (e: FileNotFoundException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            val dec = decrypt(bytes, "1234")

            val bos = BufferedOutputStream(FileOutputStream(Environment.getExternalStorageDirectory().absolutePath + "/A/3.jpg"))
            bos.write(dec)
            bos.flush()
            bos.close()
        }.start()
    }


    fun testCompression(dataIn: String) {
        var data = dataIn
        logger.info { "testCompression" }

        val time = System.currentTimeMillis()

        logger.info { "message: " + data.length }
        val compressed = compress(data)
        logger.info { "compressed: " + compressed.size }
        logger.info { "compressed: " + String(compressed, Charset.forName("utf-8")) }

        data = decompress(compressed)
        logger.info { "decompressed: " + data.length }
        logger.info { "decompressed: $data" }

        logger.info { "testCompression->time: " + (System.currentTimeMillis() - time) }
    }

    fun printByteArray(data: ByteArray) {
        logger.info { "---------" }
        for (b in data) {
            logger.info { b }
        }
        logger.info { "=========" }

    }

    fun encrypt(path: String, password: String): String? {
        if (password.isEmpty()) return null
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val spec = PBEKeySpec(password.toCharArray(), salt, 100, 128) // AES-256
        val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

        val keyBytes = f.generateSecret(spec).encoded
        val key = SecretKeySpec(keyBytes, "AES")
        logger.info { "PASSWORD: $password" }

        logger.info { "SALT: " + toBase64(salt) }
        logger.info { "key: " + toBase64(key.encoded) }

        val ivBytes = ByteArray(16)
        random.nextBytes(ivBytes)

        val iv = IvParameterSpec(ivBytes)

        val ivSalt = ByteArray(32)
        System.arraycopy(ivBytes, 0, ivSalt, 0, 16)
        System.arraycopy(salt, 0, ivSalt, 16, 16)


        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, key, iv)


        val dest = random.nextInt(9999)
        val fileOutputStream = FileOutputStream("${Environment.DIRECTORY_DCIM}/$dest")
        fileOutputStream.write(ivSalt)
        fileOutputStream.flush()

        val cos = CipherOutputStream(fileOutputStream, c)
        val isa: InputStream = FileInputStream(path) //Input stream
        ByteStreams.copy(isa, cos)
        isa.close()
        cos.close()
        return dest.toString()
    }

    fun encrypt(plaintext: ByteArray, password: String): ByteArray {
        if (password.isEmpty()) return ByteArray(0)
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val spec = PBEKeySpec(password.toCharArray(), salt, 100, 128) // AES-256
        val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

        val keyBytes = f.generateSecret(spec).encoded
        val key = SecretKeySpec(keyBytes, "AES")
        logger.info { "PASSWORD: $password" }

        logger.info { "SALT: " + toBase64(salt) }
        logger.info { "key: " + toBase64(key.encoded) }

        val ivBytes = ByteArray(16)
        random.nextBytes(ivBytes)

        val iv = IvParameterSpec(ivBytes)

        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, key, iv)
        val encValue = c.doFinal(plaintext)

        val finalCipherText = ByteArray(encValue.size + 2 * 16)
        System.arraycopy(ivBytes, 0, finalCipherText, 0, 16)
        System.arraycopy(salt, 0, finalCipherText, 16, 16)
        System.arraycopy(encValue, 0, finalCipherText, 32, encValue.size)
        return finalCipherText


    }

    fun decrypt(data: ByteArray, pre_shared_key: String): ByteArray {
        if (pre_shared_key.isEmpty()) return ByteArray(0)

        try {
            val ivBytes = ByteArray(16)
            val salt = ByteArray(16)
            val cipherBytes = ByteArray(data.size - 2 * 16)

            System.arraycopy(data, 0, ivBytes, 0, 16)
            System.arraycopy(data, 16, salt, 0, 16)
            System.arraycopy(data, 32, cipherBytes, 0, data.size - 2 * 16)

            val spec = PBEKeySpec(pre_shared_key.toCharArray(), salt, 100, 128) // AES-256
            val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

            val keyBytes = f.generateSecret(spec).encoded
            val key = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivParams = IvParameterSpec(ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams)
            return cipher.doFinal(cipherBytes)

        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return ByteArray(0)
    }

    fun compress(string: String): ByteArray {
        try {
            val os = ByteArrayOutputStream(string.length)
            val gos: GZIPOutputStream?
            gos = GZIPOutputStream(os)
            gos.write(string.toByteArray())
            gos.close()
            val compressed = os.toByteArray()
            os.close()
            return compressed
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ByteArray(0)
    }

    fun compress(content: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(String(content)) }
        return bos.toByteArray()
    }

    fun decompress(content: ByteArray): String =
            GZIPInputStream(content.inputStream()).bufferedReader(UTF_8).use { it.readText() }

    fun pack(data: String, pre_shared_key: String?): String {
        if (pre_shared_key.isNullOrEmpty()) return ""

        val compressed = compress(data)
        val encrypted = if (pre_shared_key != null)
            encrypt(compressed, pre_shared_key)
        else
            compressed
        return toBase64(encrypted)
    }

    fun pack(data: ByteArray, pre_shared_key: String?): ByteArray {
        if (pre_shared_key.isNullOrEmpty()) return ByteArray(0)

        val compressed = compress(data)
        return if (pre_shared_key != null)
            encrypt(compressed, pre_shared_key)
        else
            compressed
    }

    fun unpack(data: String, pre_shared_key: String?): String {
        if (pre_shared_key.isNullOrEmpty()) return ""
        val enc = fromBase64(data)
        val compressed = if (pre_shared_key != null)
            decrypt(enc, pre_shared_key)
        else
            enc
        return decompress(compressed)
    }


    fun toBase64(data: ByteArray): String = Base64.encodeToString(data, Base64.DEFAULT)

    fun fromBase64(data: String): ByteArray = Base64.decode(data, Base64.DEFAULT)

}