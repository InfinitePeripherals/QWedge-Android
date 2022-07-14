---
layout: default
title: Quick Start
nav_order: 1
has_children: false
---

# Q Wedge - Quick Start (Android) 
{: .fs-9 .no_toc }

Q Wedge is a data wedge that can send scanned barcode and captured images to the target apps via keystrokes and intents.
{: .fs-5 .fw-300 }

Version 1.0.31
{: .fs-5 .fw-300 }

---
<details open markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
1. TOC
{:toc}
</details>
---

## App Settings

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/BarcodeService.png)
### Barcode Service
*Barcode Service* is a foreground service that handles the Barcode Engine and listens for intents to act on the commands it receives. This service **must** be enabled for Q Wedge to start the Barcode Engine and work properly.

### Barcode Engine
A Barcode Engine that you want to use with the Android device.

If your device is Halo, the barcode engine will be activated when Barcode Service is turned on without any pairing needed.

If your device is not a Halo, tap the *CONNECT SCANNER* button to find and connect to supported BLE barcode device (i.e.: Nexus Connect). 


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/BarcodeSettings.png)
### Barcode Settings
#### Scan Beep
When enabled, a beep will sound after each successful barcode scan, and when an image is captured.

#### Hardware Button Trigger
This setting allows the scan button (keycode: 131) to start/stop the scanner engine automatically when pressed or release.

#### Barcode Filter
Filter barcodes based on a JavaScript rule set. This script can be updated via MDM or intent.


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/KeyboardOutputMode.png)
### Keyboard Output Mode
When enabled, Q Wedge will be able to inject scanned barcode to the active text field's cursor as keystrokes using the Accessibility mode. 

In order for Keyboard Output Mode to work, the Accessibility mode must be enabled for Q Wedge app as follow:

*Go to Android Settings > Accessibility > Q Wedge Input > turn on Use Service.*

#### Insert Return
Insert a new line after a barcode is scanned.

#### Overwrite Current Text
Replace the current text of the active text field with the scanned barcode.

#### Keys Suggestion
When this option is enabled, hold down a key on the Q Wedge keyboard will present a suggestion for the keys surrounding. 

For example: when holding down the 'A' key, a suggestion will popup with the keys surrounding: A, S, Q, W, Z

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeySuggestion.png)


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/IntentOutputMode.png)

### Intent Output Mode
This mode enabled Q Wedge's Barcode Service to broadcast the scanned barcode, and captured image to any apps that registered for the custom intent actions. Tap on "Tap for Intent Details" to see a list of the available intents that can be registered in your app.

The default intents for receiver are as follow:
- Intent Action: `com.ipc.qwedge.intent.ACTION`
- Barcode Data Intent Extra: `com.ipc.qwedge.intent.barcodeData`
- Barcode Type Intent Extra: `com.ipc.qwedge.intent.barcodeType`
- Image URI Intent Extra: `com.ipc.qwedge.intent.image`

#### Active App Only
When this setting is ON, barcodes only broadcast to the active app on the foreground. 

This setting relies on Accessibility mode to be ON. (Default is On when using Halo)

*Go to Android Settings > Accessibility > Q Wedge Input > turn on Use Service.*

## Intent API
The intent API let any app send an intent with a command to Q Wedge like start/stop the scan engine or capture an image, then the result intent will be sent to the app via the defined Intent API.

### Intent Action
All apps that want to send commands to Q Wedge must use `com.ipc.qwedge.api.ACTION` as the intent action.

#### Barcode Commands
The barcode scanner engine can be triggered via intent API as below:
```kotlin=
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

To capture image, the app needs to start the camera Activity using `startActivityForResult()` Once an image is captured and saved, the image path will be returned with the result's data intent. You can then use `FileInputStream` to open input stream using the path to retrieve the image data, and convert to Bitmap.

```kotlin=
// Setup activity launcher
// Once the image is captured, the closure will be called with the result which contain the intent data with the image path.
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

// Launch camera view using Intent
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
- `resultLauncher`: The ActivityResultLauncher to launch camera activity. This should be setup once.
- `startActivityIntent`: The intent to launch camera activity with the action set to `com.ipc.qwedge.api.LAUNCH_CAMERA`. Once the camera activity is launched, you can capture the image by pressing the physical scan (middle) button, or press either the left or right volume button to cancel and close the camera activity.
- `result`: Once the image is captured, the result intent is stored in `result.data` intent. The image path is stored in the `com.ipc.qwedge.intent.image` extra. To retrieve the image, create a `File` with the given path, then use `FileInputStream` to read the image file.

## Set Configuration
Q Wedge's configuration can be updated via `Intent` or MDM. Below is a list of keys that can be configured:

```kotlin=
/// Enable or Disabld Barcode Service
const val enableBarcodeService = "enableBarcodeService"

/// Enable or Disable barcode filter script
const val enableBarcodeFilterScript = "enableBarcodeFilterScript"

/// The JS rule that will be used to filter barcodes.
const val activeFilter = "activeFilter"

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

### Set Configuration via Intent
Below is an example to set configuration via Intent from another app:
```kotlin=
// Create a bundle that contains config values.
var config = Bundle()
// Enable scan beep
config.putBoolean("enableScanBeep", true)
// Enable hardware scan button
config.putBoolean("enableHardwareButtonTrigger", true)

// ... Some other configs ...

// Setup intent with config
Intent().also {
    it.action = "com.ipc.qwedge.api.ACTION"
    it.putExtra("com.ipc.qwedge.api.SET_CONFIG", config)
    sendBroadcast(it)
}
```



## Smart Rules (Barcode Filter)
Q Wedge supports JavaScript rules to filter out unwanted barcodes. You can write a JavaScript file to process the scanned barcode to determined if it should be accepted or rejected. If it is accepted, it would be sent to keyboard, or broadcast via intent to all the apps that are setup to receive barcodes.

### Create JavaScript File
You can write the JavaScript rule as normal as any other JavaScript files. There are 3 important rules to follow for the script to work:
1. The main JavaScript function must take in 2 parameters:
    - Symbology (String)
    - Barcode (String)

    Example: `MainFunction(symbology, barcode)`
    
2. The function must return an object with format as follow: 

    ```kotlin=
    { 
        accept: Boolean, 
        adjBarcode: String 
    }
    ```
    - `accept`: the value should be a boolean, that tell Q Wedge if the scanned barcode is accepted and passed the validation.
    - `adjBarcode`: If your script need to modify barcode, you can set the modified barcode string value here, otherwise return the original barcode. 
    
3. The JavaScript file name must match the main function name. 

    Example: if your main function is called `MainFunction`, then your JavaScript file name should be `MainFunction.js`
    
### JavaScript File Location
The JavaScript file must be copied to this specified public location so Q Wedge can look for it: 
`..Internal shared storage/Documents/QWedge/Scripts/`

### Activate Smart Rule
To use Smart Rules, you need to enable `Barcode Filter` either within the Q Wedge app or via `Intent`, and send an `Intent` to tell Q Wedge to use the JavaScript file that you just created.

Below is how you would enable `Barcode Filter`, and set the active JavaScript rule that Q Wedge should use to process barcodes via `Intent`:

```kotlin=
// Create a bundle that contains config values.
var config = Bundle()
// Enable Barcode Filter
config.putBoolean("enableBarcodeFilterScript", true)
// Set the active filter script
config.putString("activeFilter", "MainFunction")

// Setup intent with config and send it.
Intent().also {
    it.action = "com.ipc.qwedge.api.ACTION"
    it.putExtra("com.ipc.qwedge.api.SET_CONFIG", config)
    sendBroadcast(it)
}

```


## Q Wedge Keyboard

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeyboard.png)\

Q Wedge comes with a custom built keyboard. On the Halo device, Q Wedge keyboard is the default keyboard. On other devices, you need to turn it on via Languages & Input in Android Settings.\


### Magnify Keys

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeyMagnify.png)\

When `Keys Suggestion` is disbaled, tap & hold on the keyboard keys will display a magnified popup of that key. From there you can drag your finger across the keyboard, and let go on the key that you want to enter.\


### Keys Suggestion Mode

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeySuggestion.png)\

When `Keys Suggestion` is enabled, tap & hold down on a key will display a popup with suggested surrounding keys with bigger button size for easy to tap.\


### Numeric Keyboard
#### Number Pad\

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/NumericPadKeyboard.png)\

The default numeric keyboard on the Halo device is the number pad for easy typing. To switch to number pad, just tap the `123` key.\

#### Full Number & Symbol Keyboard

To switch to the full Number & Symbol keyboard, tap and hold the `123` key and select `?123` key\

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/NumericKeyboardOptions.png)\

The full Number & Symbol keyboard looks like below:\

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/NumericFullKeyboard.png)\


### Function Keyboard

Q Wedge also provides a Function keyboard. Tap the `Fn` key to activate Function keyboard\

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/FunctionKeyboard.png)\
