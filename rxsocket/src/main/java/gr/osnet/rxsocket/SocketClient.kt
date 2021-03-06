package gr.osnet.rxsocket

import gr.osnet.rxsocket.meta.*
import gr.osnet.rxsocket.post.AsyncPoster
import gr.osnet.rxsocket.post.IPoster
import gr.osnet.rxsocket.post.SyncPoster
import io.reactivex.Observable
import mu.KotlinLogging
import java.io.*
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


private val logger = KotlinLogging.logger {}

class SocketClient(private val mConfig: SocketConfig) {

    var mSocket: Socket = Socket()
    private var mOption: SocketOption? = null
    lateinit var mObservable: Observable<DataWrapper>
    lateinit var mIPoster: IPoster
    var mExecutor: Executor = Executors.newCachedThreadPool()
    var lastExchange: Long = System.currentTimeMillis()
    fun option(option: SocketOption): SocketClient {
        mOption = option
        return this
    }


    fun connect(): Observable<DataWrapper> {
        connectedTime = System.currentTimeMillis()
        mObservable = SocketObservable(mConfig, mSocket, this, mOption)
        mIPoster = if (mConfig.mThreadStrategy == ThreadStrategy.ASYNC) AsyncPoster(this, mExecutor) else SyncPoster(this, mExecutor)
        initHeartBeat()
        return mObservable
    }

    fun disconnect() {
        if (mObservable is SocketObservable) {
            (mObservable as SocketObservable).close(Throwable(""))
        }
    }

    fun disconnectWithError(throwable: Throwable) {
        if (mObservable is SocketObservable) {
            (mObservable as SocketObservable).state = SocketState.CLOSE_WITH_ERROR
            (mObservable as SocketObservable).close(throwable)
        }
    }

    private fun initHeartBeat() {
        mOption?.apply {
            if (mHeartBeatConfig != null) {
                val disposable = Observable.interval(mHeartBeatConfig.interval / 2, TimeUnit.MILLISECONDS)
                        .subscribe {
                            when {
                                shouldSendHeartBeat() -> sendData(mHeartBeatConfig.data
                                        ?: ByteArray(0))
                            }
                        }
                if (mObservable is SocketObservable) {
                    (mObservable as SocketObservable).setHeartBeatRef(disposable)
                }
            }
        }
    }

    private fun shouldSendHeartBeat(): Boolean {
        mOption?.apply {
            val deltaTime = System.currentTimeMillis() - lastExchange
            return mHeartBeatConfig != null && deltaTime > mHeartBeatConfig.interval
        }
        return false
    }

    private fun concatByteArray(array1: ByteArray, array2: ByteArray, array3: ByteArray): ByteArray {
        val aLen = array1.size
        val bLen = array2.size
        val cLen = array3.size
        val result = ByteArray(aLen + bLen + cLen)

        System.arraycopy(array1, 0, result, 0, aLen)
        System.arraycopy(array2, 0, result, aLen, bLen)
        System.arraycopy(array3, 0, result, bLen + 1, cLen)

        return result
    }

    private fun concatByteArray(array1: ByteArray, array2: ByteArray): ByteArray {
        val aLen = array1.size
        val bLen = array2.size
        val result = ByteArray(aLen + bLen)

        System.arraycopy(array1, 0, result, 0, aLen)
        System.arraycopy(array2, 0, result, aLen, bLen)

        return result
    }


    private fun addHeadTail(data: ByteArray): ByteArray {
        mOption?.apply {
            when (hasHeadTail()) {
                HeadTail.BOTH -> return concatByteArray(ByteArray(1) { mHead!! }, data, ByteArray(1) { mTail!! })
                HeadTail.HEAD_ONLY -> return concatByteArray(ByteArray(1) { mHead!! }, data)
                HeadTail.TAIL_ONLY -> return concatByteArray(data, ByteArray(1) { mTail!! })
            }
        }
        return data
    }

    private fun addCRC(data: ByteArray): ByteArray {
        mOption?.apply {
            if (mHasCRC)
                return CRC16Hex.addCheck(String(data)).toByteArray()
        }
        return data
    }

    private fun pack(data: ByteArray): ByteArray {
        mOption?.apply {
            val result = AES.pack(String(data), mPreSharedKey).toByteArray(charset = mConfig.mCharset)
            return ("ENC^" + String(result)).toByteArray(charset = mConfig.mCharset)
        }
        return data
    }

    fun encrypt(data: ByteArray): ByteArray? {
        return mOption?.mPreSharedKey?.let { AES.encrypt(data, it) }
    }

    fun sendBytes(data: ByteArray?, encrypted: Boolean = false) {
        if (data == null)
            return
        val result: ByteArray = if (encrypted)
            mOption?.mPreSharedKey?.let { AES.encrypt(data, it) }!!
        else
            data

        mIPoster.enqueue(result)
        logger.info { "To server : ByteArray ->" + result.size / 1000 + "kb" }

        lastExchange = System.currentTimeMillis()
    }

    fun encrypt(path: String): String? {
        return mOption?.mPreSharedKey?.let { AES.encrypt(path, it) }
    }


    fun sendBytes(path: String?, encrypted: Boolean = false) {
        if (path == null)
            return
        val dest: String? = if (encrypted)
            mOption?.mPreSharedKey?.let { AES.encrypt(path, it) }
        else
            path

        val file = File(dest)
        val size = file.length()
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            val bufferSize = 15 * 1024 * 1024
            val result = ByteArray(bufferSize)
            logger.info { "To server file size: ByteArray ->$size" }
            while (buf.available() > 0) {
                logger.info { "=======" }
                val available = buf.available()
                val toSend = if (available < bufferSize) {
                    buf.read(result, 0, available)
                    result.copyOf(available)
                } else {
                    buf.read(result, 0, bufferSize)
                    result.copyOf(bufferSize)
                }
                mSocket.getOutputStream()?.apply {
                    try {
                        write(toSend)
                        flush()
                    } catch (throwable: Throwable) {
                        disconnectWithError(throwable)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        logger.info { "To server : ByteArray ->" + size / 1000 + "kb" }
        lastExchange = System.currentTimeMillis()
    }

    fun sendData(data: ByteArray?, encrypted: Boolean = false) {
        if (data == null)
            return
        val result: ByteArray = if (encrypted)
            pack(data)
        else
            data

        val result2 = addCRC(result)
        val result3 = addHeadTail(result2)

        mIPoster.enqueue(result3)
        logger.info { "To server Packed: " + String(result3) }

        lastExchange = System.currentTimeMillis()
    }

    fun sendData(message: String?, encrypted: Boolean = false) {
        logger.info { "To server Unpacked: $message" }
        sendData(message?.toByteArray(charset = mConfig.mCharset), encrypted)
    }

    fun sendEnd() {
        logger.info { "To server: END" }
        sendData("END")
    }

    fun waitUntilEnd() {
        while (!(mObservable as SocketObservable).isClosed)
            Thread.sleep(400)
    }

    companion object {
        var connectedTime: Long = System.currentTimeMillis()
    }

}