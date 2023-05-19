package com.ipc.qwedgedemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.core.view.KeyEventDispatcher
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.ipc.qwedge.intent.ACTION")
        registerReceiver(broadcastReceiver, intentFilter)

        // ========= Setup activity laucnher once
        this.resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // parse result and perform action
                val imageData = result.data?.getStringExtra("com.ipc.qwedge.intent.image")
                if (imageData != null) {
                    this.displayImage(imageData)
                }
            }
        }
        // ==================================

        this.requestAllPermissions()
    }

    private var broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.ipc.qwedge.intent.ACTION") {
                val extras = intent!!.extras
                if (extras == null) {
                    return
                }

                if (extras.containsKey("com.ipc.qwedge.intent.barcodeData")) {
                    val barcodeData = extras.getString("com.ipc.qwedge.intent.barcodeData")
                    if (barcodeData != null) {
                        if (extras.containsKey("com.ipc.qwedge.intent.barcodeType")) {
                            val barcodeType = extras.getString("com.ipc.qwedge.intent.barcodeType")
                            if (barcodeType != null) {
                                // Do something ...
                                val intentBarcodeEditText = findViewById<EditText>(R.id.intentEditText)
                                if (intentBarcodeEditText != null) {
                                    intentBarcodeEditText.setText("${barcodeData!!} - ${barcodeType}")

                                }
                            }
                        }
                    }
                }

                if (extras.containsKey("com.ipc.qwedge.intent.image")) {
                    val imageData = extras.getString("com.ipc.qwedge.intent.image")
                    if (imageData != null) {
                        // Retrieve the image from path and display it
                        this@MainActivity.displayImage(imageData)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.updateConfig()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // We launch the camera by pressing the right volume button
        if (keyCode == 25) {
            this.launchCamera()
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

    fun updateConfig() {
        val config = Bundle()
        // Enable scan beep
        config.putBoolean("enableScanBeep", true)
        // Enable hardware scan button
        config.putBoolean("enableHardwareButtonTrigger", true)
        // Set active app bundle
        config.putString("packageName", this.packageName)

        Intent().also {
            it.action = "com.ipc.qwedge.api.ACTION"
            it.putExtra("com.ipc.qwedge.api.SET_CONFIG", config)
            sendBroadcast(it)
        }
    }

    // Get the image data and show it on a view
    fun displayImage(image: String) {
        try {
            val file = File(image)
            val inputStream = FileInputStream(file)
            val imageBytes = inputStream?.readBytes()
            if (imageBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    // Show on imageView
                    val imageView = findViewById<ImageView>(R.id.photoImageView)
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap)
                    }
                }
            }

            inputStream?.close()
        }
        catch (e: Exception) {
            Log.d("QWedgeDemo", "Failed to get image: ${e.message}")
        }
    }

    fun launchCamera() {
        // Send
        val intent = Intent()
        intent.action = "com.ipc.qwedge.api.LAUNCH_CAMERA"
        intent.putExtra("com.ipc.qwedge.api.PACKAGE_NAME", this.packageName)
        this.resultLauncher!!.launch(intent)
    }

    // Request permission for disk access
    private fun requestAllPermissions() {
        // Check for permission
        var permissions = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.count() > 0) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
        }
    }
}