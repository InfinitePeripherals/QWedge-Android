# Q Wedge - Quick Start (Android) 

Q Wedge is a data wedge that can send scanned barcode and captured images to the target apps via keystrokes and intents.

Version 1.0.30

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

#### Barcode Filter
Filter barcodes based on a JavaScript rule set. This script can be updated via MDM or intent.

### Keyboard Output Mode
When enabled, Q Wedge will be able to inject scanned barcode to the active text field's cursor as keystrokes using the Accessibility mode. 

In order for Keyboard Output Mode to work, the Accessibility mode must be enabled for Q Wedge app as follow:

*Go to Android Settings > Accessibility > Q Wedge Input > turn on Use Service.*

#### Overwrite Current Text
Replace the current text of the active text field with the scanned barcode.

### Intent Output Mode
This mode enabled Q Wedge's Barcode Service to broadcast the scanned barcode, and captured image to any apps that registered for the custom intent actions. Tap on "Tap for Intent Details" to see a list of the available intents that can be registered in your app.

The default intents for receiver are as follow:
- Intent Action: `com.ipc.qwedge.intent.ACTION`
- Barcode Data Intent Extra: `com.ipc.qwedge.intent.barcodeData`
- Barcode Type Intent Extra: `com.ipc.qwedge.intent.barcodeType`
- Image URI Intent Extra: `com.ipc.qwedge.intent.image`

#### Active App Only
When this setting is ON, barcodes only broadcast to the active app on the foreground. 

This setting relies on Accessibility mode to be ON. 

*Go to Android Settings > Accessibility > Q Wedge Input > turn on Use Service.*

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
The scanner engine camera can capture an image and save it to disk, then broadcast its location to receivers. 

To capture image, the app needs to start the camera Activity using `startActivityForResult()` Once an image is captured and saved, the image path will be returned with the result's data intent. You can then use FileInputStream to open input stream using the path to retrive the image data, and convert to Bitmap.

```
// Setup launcher
var resultLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        // parse result
        val imagePath = result.data?.getStringExtra("com.ipc.qwedge.intent.image")
        if (imagePath != null) {
            // Handle image path
            this.displayImage(imagePath)
        }
    }
}

// Launch camera
val startActivityIntent = Intent()
startActivityIntent.action = "com.ipc.qwedge.api.LAUNCH_CAMERA"
resultLauncher.launch(startActivityIntent)

// Get the image data and show it on a view
fun displayImage(imagePath: String) {
    val file = File(image)
    val inputStream = FileInputStream(file)
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
- startActivityIntent: The intent to launch camera activity with the action set to `com.ipc.qwedge.api.LAUNCH_CAMERA`. Once the camera activity is launched, you can capture the image by pressing the scan button, or press either the left or right volume button to cancel and close the camera activity.
- result: Once the image is captured, the result intent is stored in `result.data` intent. The image path is stored in the `com.ipc.qwedge.intent.image` extra. To retrieve the image, create a `File` with the given path, then use `FileInputStream` to read the image file.

## Set Configuration
QWedge supports configuration update via Intents. Below is a list of keys that can be configured:

```
/// Enable or Disabld Barcode Service
const val enableBarcodeService = "enableBarcodeService"

/// Enable or Disable barcode filter script
const val enableBarcodeFilterScript = "enableBarcodeFilterScript"

/// The JS rule that will be used to filter barcodes.
const val barcodeFilterScript = "barcodeFilterScript"

/// Enable or Disable scan beep when a barcode is scanned.
const val enableScanBeep = "enableScanBeep"

/// Enable or Disable hardware button trigger. If enabled, the center button will be the scan button. If disabled, you can control the scan engine via Intent.
const val enableHardwareButtonTrigger = "enableHardwareButtonTrigger"

/// Enable or Disable keyboard output mode. When enabled, and a text field is in focus, the barcode will be injected into the text field.
const val enableKeyboardOutputMode = "enableKeyboardOutputMode"

/// Enable or Disable insert return at the end of the scanned barcode.
const val enableInsertReturn = "enableInsertReturn"

/// Enable or Disable overwrite the current content in the text field when a new barcode is scanned.
const val enableOverwriteCurrentText = "enableOverwriteCurrentText"

/// Enable or Disable keys suggestion on the QWedge keyboard layout.
/// When a key on the keyboard held down for a brief moment, a popup will be shown with suggested keys around the key that being held down.
const val enableKeysSuggestion = "enableKeysSuggestion"

/// Enable or Disable intent output mode. This mode should be ON in order to receive the barcode via intents.
const val enableIntentOutputMode = "enableIntentOutputMode"

/// The main intent API action. This action is used in the intent broadcast response to external app.
/// By default it is set to "com.ipc.qwedge.intent.ACTION", but you can change it to other string that your app already filtering for.
const val intentAction = "intentAction"

/// The barcode data extra hold the intent extra key that contains the barcode data on intent broadcast.
/// If your app is already has an extra key set for the barcode data, you can set this value to match with your app.
const val barcodeDataExtra = "barcodeDataExtra"

/// The barcode type extra hold the intent extra key that contains the barcode type on intent broadcast.
/// If your app is already has an extra key set for the barcode type, you can set this value to match with your app.
const val barcodeTypeExtra = "barcodeTypeExtra"

/// The barcode type text extra hold the intent extra key that contains the barcode type text on intent broadcast.
/// If your app is already has an extra key set for the barcode type text, you can set this value to match with your app.
const val barcodeTypeTextExtra = "barcodeTypeTextExtra"

/// The image extra that hold the image path in the activity result.
const val imageExtra = "imageExtra"

/// The settings parameters to turn on/off symbologies.
const val parameters = "parameters"

/// Enable or Disable intent broadcast response to the active app on foreground only. 
/// When disabled, any app register to receive barcode data via intent will receive the broadcast even in background.
const val enableActiveAppOnly = "enableActiveAppOnly"
```

### Set configuration via Intent
Below is an example to set configuration via Intent from another app:
```
// Create a bundle that contains config values.
var config = Bundle()
config.putBoolean("enableScanBeep", true)
config.putBoolean("enableHardwareButtonTrigger", true)

// Setup intent with config
Intent().also {
    it.action = "com.ipc.qwedge.api.ACTION"
    it.putExtra("com.ipc.qwedge.api.SET_CONFIG", config)
    sendBroadcast(it)
}
```
