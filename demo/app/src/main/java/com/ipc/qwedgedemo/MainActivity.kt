package com.ipc.qwedgedemo

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.ipc.qwedge.intent.ACTION")
        registerReceiver(broadcastReceiver, intentFilter)

        this.resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // parse result and perform action
                val imageURI = result.data?.getStringExtra("com.ipc.qwedge.intent.image")
                if (imageURI != null) {
                    this.displayImage(imageURI)
                }
            }
        }

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
                        val intentBarcodeEditText = findViewById<EditText>(R.id.intentEditText)
                        if (intentBarcodeEditText != null) {
                            intentBarcodeEditText.setText("${barcodeData!!}")
                        }
                    }
                }

                if (extras.containsKey("com.ipc.qwedge.intent.barcodeType")) {
                    val barcodeType = extras.getString("com.ipc.qwedge.intent.barcodeType")
                    if (barcodeType != null) {
                        // Do something ...
                    }
                }
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // If the left or right button pressed, launch camera view
        if (keyCode == 24 || keyCode == 25) {
            this.launchCamera()
            return false
        }

        return super.onKeyUp(keyCode, event)
    }

    // Get the image data and show it on a view
    fun displayImage(imageURI: String) {
        val inputStream = contentResolver.openInputStream(Uri.parse(imageURI))
        val imageBytes = inputStream?.readBytes()
        if (imageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            if (bitmap != null) {
                val imageView = findViewById<ImageView>(R.id.photoImageView)
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }

        inputStream?.close()
    }

    fun launchCamera() {
        val intent = Intent()
        intent.action = "com.ipc.qwedge.api.LAUNCH_CAMERA"
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