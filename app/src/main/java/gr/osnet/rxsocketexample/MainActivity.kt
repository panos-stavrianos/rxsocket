package gr.osnet.rxsocketexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun checkPermission() {// Here, thisActivity is the current activity
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this,
                            permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()

        //Communication.scheduleWorker()
        Communication.fireWorker()

        /*   val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/1.mp4"
           val encPath: String? = AES.encrypt(path, "1234")
           val decPath = AES.decrypt(encPath!!, "1234")
           Log.e("MainActivity", decPath)
        */   //   AES.testingStuff()
    }
}
