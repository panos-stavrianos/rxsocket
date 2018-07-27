package gr.osnet.rxsocketexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.*
import gr.osnet.rxsocket.RxSocketClient
import gr.osnet.rxsocket.SocketSubscriber
import gr.osnet.rxsocket.meta.SocketConfig
import gr.osnet.rxsocket.meta.SocketOption
import gr.osnet.rxsocket.meta.ThreadStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit


/**
 * Created by panos on 8/2/2016.
 */
val Context.imei: String
    @SuppressLint("HardwareIds")
    get() = when {
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED -> ""
        else -> (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
    }

class Communication : Worker() {


    private var disposables = CompositeDisposable()


    override fun doWork(): Result {
        Log.e(TAG, "doWork")

        disposables.clear()
        val host = "192.168.1.12"//"94.70.250.186"
        val port = 30010
        val key = "1234"
        Log.e(TAG, "Try to connect to $host:$port")

        val appName = "ONSAFE^3~${applicationContext.imei}"

        val mClient = RxSocketClient
                .create(SocketConfig.Builder()
                        .setIp(host)
                        .setPort(port)
                        .setCharset(Charsets.UTF_8)
                        .setThreadStrategy(ThreadStrategy.SYNC)
                        .setTimeout(5 * 1000)
                        .build())
                .option(SocketOption.Builder()
                        .setHeartBeat("beep".toByteArray(), 5 * 1000)
                        .setPreSharedKey(key)
                        .hasCRC(false)
                        .setHead(HEAD)
                        .setTail(TAIL)
                        .setFirstContact(appName)
                        .build())

        val time = System.currentTimeMillis()
        Log.e(TAG, "before connect")

        mClient.connect()
                .subscribe(
                        object : SocketSubscriber() {
                            override fun onConnected() {
                                Log.e(TAG, "onConnected")
                                val encrypted = mClient.encrypt(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/1.jpg")
                                mClient.sendBytes(encrypted, false)
                            }

                            override fun onDisconnected(timePassed: Long) {
                                Log.e(TAG, "onDisconnected: " + (System.currentTimeMillis() - time) + " millis")
                                complete()
                            }

                            override fun onDisconnectedWithError(throwable: Throwable, timePassed: Long) {
                                Log.e(TAG, "onDisconnectedWithError in ${TimeUnit.MILLISECONDS.toSeconds(timePassed)} sec, cause: ${throwable.message}")
                                complete()
                            }

                            override fun onResponse(data: String, timePassed: Long) {
                                Log.e(TAG, "onResponse in ${TimeUnit.MILLISECONDS.toSeconds(timePassed)} sec: $data")
                                if (data == "END")
                                    mClient.disconnect()
                            }

                        }, Consumer {
                    Log.e(TAG, "Communication throwable")
                    it.printStackTrace()
                }, Action { Log.e(TAG, "Communication completed") })?.let { disposables.add(it) }
        mClient.waitUntilEnd()
        Log.e(TAG, "return Worker.Result.SUCCESS!!!!!!")
        return Worker.Result.SUCCESS
    }

    fun complete() {
        Log.e(TAG, "complete")
        // disposables.clear()
    }


    companion object {

        private val TAG = Communication::class.java.simpleName
        //private static final byte[] HEART_BEAT = "beep".getBytes();
        private const val HEAD: Byte = 2
        private const val TAIL: Byte = 3
        private val myConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build()
        private var communicationWork: OneTimeWorkRequest = OneTimeWorkRequestBuilder<Communication>()
                .setConstraints(myConstraints)
                .build()

        fun scheduleRepeaterWorker() {
            Log.e(TAG, "scheduleRepeaterWorker")

            WorkManager.getInstance().let {
                val myConstraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED).build()

                val communicationWork =
                        PeriodicWorkRequestBuilder<Communication>(15, TimeUnit.MINUTES)
                                .setConstraints(myConstraints)
                                .build()

                val compressionWorkId = communicationWork.id
                it.cancelWorkById(compressionWorkId)

                it.enqueue(communicationWork)
            }
        }

        fun fireWorker() {
            Log.e(TAG, "fireWorker")

            WorkManager.getInstance().let {
                it.cancelAllWork()
                communicationWork = OneTimeWorkRequestBuilder<Communication>()
                        .setConstraints(myConstraints)
                        .build()
                it.enqueue(communicationWork)
            }

        }
    }
}
