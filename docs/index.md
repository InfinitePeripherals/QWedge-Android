# Q Wedge - Quick Start (Android) 

Q Wedge is a data wedge that can send scanned barcode and captured images to the target apps via keystrokes and intents.

Version 1.0.9

## App Configuration

### Barcode Service
*Barcode Service* is a foreground service that handles the Barcode Engine and listens for intents to act on the commands it receives. This service **must** be enabled for Q Wedge to start the Barcode Engine and work properly.

### Barcode Engine
A Barcode Engine that you want to use with the Android device.

If your device is Ring Scanner, the barcode engine will be activated when Barcode Service is turned on without any pairing needed.

If your device is not a Ring Scanner, tap the *CONNECT SCANNER* button to find and connect to supported BLE barcode device (i.e.: Nexus Connect). 

### Barcode Settings
#### Scan Beep
When enabled, a beep will sound after each successful barcode scan, and when an image is captured.

#### Hardware Button Trigger
This setting allows the scan button (keycode: 131) to start/stop the scanner engine automatically when pressed or release.

### Keyboard Output Mode
When enabled, Q Wedge will be able to inject scanned barcode to the active text field's cursor as keystrokes using the Accessibility mode. 

In order for Keyboard Output Mode to work, the Accessibility mode must be enabled for Q Wedge app as follow:

*Go to Android Settings > Accessibility > Q Wedge Input > turn on Use Service.*

#### Overwrite Text
Replace the current text of the active text field with the scanned barcode.

### Intent Output Mode
This mode enabled Q Wedge's Barcode Service to broadcast the scanned barcode, and captured image to any apps that registered for the custom intent actions. Tap on "Tap for Intent Details" to see a list of the available intents that can be registered in your app.

The default intents for receiver are as follow:
- Intent Action: `com.ipc.qwedge.intent.ACTION`
- Barcode Data Intent Extra: `com.ipc.qwedge.intent.barcodeData`
- Barcode Type Intent Extra: `com.ipc.qwedge.intent.barcodeType`
- Image URI Intent Extra: `com.ipc.qwedge.intent.image`

## Intent API
The intent API let any app send an intent with a command to Q Wedge like start/stop the scan engine or capture an image.

### Intent Action
All intents that want to send commands to Q Wedge must use `com.ipc.qwedge.api.ACTION` as the intent action

#### Barcode Commands
The barcode scanner engine can be triggered via intent API as below:
```
Intent().also { intent ->
    intent.setAction("com.ipc.qwedge.api.ACTION")
    intent.putExtra("com.ipc.qwedge.api.PACKAGE_NAME", this.packageName)
    intent.putExtra("com.ipc.qwedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING")
    sendBroadcast(intent)
}
```
- Intent Action: `com.ipc.qwedge.api.ACTION`
- Intent Extra: `com.ipc.qwedge.api.PACKAGE_NAME` should be included with value of package name of the requesting app.
- Intent Extra: `com.ipc.qwedge.api.SOFT_SCAN_TRIGGER` is the key, and the value must be one of below:
    - `START_SCANNING`: starts the scanner engine.
    - `STOP_SCANNING`: stops the scanner engine.
    - `TOGGLE_SCANNING`: toggle the scanner engine, by stopping it if it is running, or start it if it is not running.

#### Image Commands
The scanner engine camera can capture an image and save it to disk, then broadcast its URI location to receivers. 

To capture image, the app need to start the camera Activity using `startActivityForResult()` Once an image is captured and saved, the URI will be returned as result's Intent data. You can then use ContentResolver to open input stream using the URI to retrive the image data, and convert to Bitmap.

```
// Setup launcher
var resultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        // parse result
        val imageURI = result.data?.getStringExtra("com.ipc.qwedge.intent.image")
        if (imageURI != null) {
            // Handle image URI
            this.displayImage(imageURI)
        }
    }
}

// Launch camera
val intent = Intent()
intent.action = "com.ipc.qwedge.api.LAUNCH_CAMERA"
resultLauncher.launch(intent)

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

```
- resultLauncher: The ActivityResultLauncher to launch camera activity. This should be setup in onCreate.
- intent: The intent to launch camera activity with the action set to `com.ipc.qwedge.api.LAUNCH_CAMERA`. Once the camera activity is launched, you can capture the image by pressing the scan button, or press either the left or right volume button to cancel and close the camera activity.
