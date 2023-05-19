---
layout: default
title: Quick Start
nav_order: 1
has_children: false
---

# QWedge - Quick Start (Android) 
{: .fs-9 .no_toc }

QWedge is a data wedge that can send scanned barcode and captured images to the target apps via keystrokes and intents.
{: .fs-5 .fw-300 }

Version 2.0.16
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
*Barcode Service* is a foreground service that handles the Barcode Engine and listens for intents to act on the commands it receives. This service **must** be enabled for QWedge to start the Barcode Engine and work properly.


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/BarcodeEngine.png)
### Barcode Engine
#### Engine
A Barcode Engine that you want to use with the Android device.
- If your device is Halo, the barcode engine will be activated when Barcode Service is turned on without any pairing needed.
- If your device is not a Halo, tap the *CONNECT SCANNER* button to find and connect to supported BLE scanner devices (i.e.: NexusConnect). 

#### Illumination Level
The intensity of the LED illumination when barcode engine is on. Default level is 5.


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/BarcodeSettings.png)
### Barcode Settings
#### Scan Beep
When enabled, a beep will sound after each successful barcode scan, and when an image is captured.

#### Hardware Button Trigger
This setting allows the scan button (keycode: 131) to start/stop the scanner engine automatically when pressed or release.

#### MagicFilters
Filter or handle the barcode using JavaScript script. This script can be updated via MDM or intent.

#### Debug Log
Enable or disable debug log.


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/KeyboardOutputMode.png)
### Keyboard Output Mode
When enabled, QWedge will be able to inject scanned barcode to the active text field's cursor as keystrokes using the Accessibility mode. 

On non-Halo device, in order for Keyboard Output Mode to work, the Accessibility mode must be enabled for QWedge app as below:

*Go to Android Settings > Accessibility > QWedge Input > turn on "Use Service".*

#### Insert Return
Insert a new line after a barcode is scanned.

#### Insert Tab
Insert a tab after a scanned barcode.

#### Overwrite Current Text
Replace the current text of the active text field with the scanned barcode.

#### Keys Suggestion
When this option is enabled, hold down a key on the QWedge keyboard will present a suggestion for the keys surrounding. 

For example: when holding down the 'A' key, a suggestion will popup with the keys surrounding: A, S, Q, W, Z

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeySuggestion.png)


![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/IntentOutputMode.png)

### Intent Output Mode
This mode enabled QWedge's Barcode Service to broadcast the scanned barcode, and captured image to any apps that registered for the custom intent actions. Tap on "Tap for Intent Details" to see a list of the available intents that can be registered in your app.

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/IntentDetails.png)

The default intents to retrieve data for receiver are as follow:
- Intent Action: `com.ipc.qwedge.intent.ACTION`
    - The default action to listen for broadcast
- Barcode Data Intent Extra: `com.ipc.qwedge.intent.barcodeData`
    - The default extra to retrieve the scanned barcode data
- Barcode Type Intent Extra: `com.ipc.qwedge.intent.barcodeType`
    - The default extra to retrieve the barcode code type
- Image URI Intent Extra: `com.ipc.qwedge.intent.image`
    - The default extra to retrieve the path of the image of the scanned barcode. The image will be deleted 15 seconds after the broadcast is sent.
- Active App Bundle: `com.company.appname`
    - The active bundle that the intent broadcast from QWedge will be sent to if Active App Only is enabled.

#### Active App Only
When this setting is enabled, barcodes only broadcast to the active app bundle. 

To set your app as the active app to receive intents, you can send a configuration command via Intent to set as below:

```kotlin=
    var config = Bundle()
    config.putString("packageName", this.packageName)

    Intent().also {
        it.action = "com.ipc.qwedge.api.ACTION"
        it.putExtra("com.ipc.qwedge.api.SET_CONFIG", config)
        sendBroadcast(it)
    }
```

It is recommended to send this configuration in your activity `onResume()` so that your app will be active app that would receive the broadcast intents from QWedge.

You can also set the Active App Bundle directly in QWedge app, by selecting *Tap for Intent details*, and change the *Active App Bundle*

## Intent API
The intent API let any app send an intent with a command to QWedge like start/stop the scan engine or capture an image, then the result intent will be sent to the app via the defined Intent API.

### Intent Action
All apps that want to send commands to QWedge must use `com.ipc.qwedge.api.ACTION` as the intent action.

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
The scanner engine camera can capture an image and save it to disk, then broadcast its location to receivers. Once the image is captured, you will have 15 seconds to retrieve it from storage, after that the image will be deleted to save space.

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
    // The action to launch camera
    startActivityIntent.action = "com.ipc.qwedge.api.LAUNCH_CAMERA"
    // Extra contains your app package name (optional)
    startActivityIntent.putExtra("com.ipc.qwedge.api.PACKAGE_NAME", this.packageName)
    // Extra contains image quality reference (optional)
    startActivityIntent.putExtra("com.ipc.qwedge.api.IMAGE_QUALITY", "LOW") // Value can be either "LOW" or "HIGH"
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
QWedge's configuration can be updated via `Intent` or MDM. Below is a list of keys that can be configured:

```kotlin=
    /// The active package name to send the intent to
    const val packageName = "packageName"

    /// Enable or Disabld Barcode Service
    /// Value is Boolean
    const val enableBarcodeService = "enableBarcodeService"

    /// Enable or Disable barcode filter script
    /// Value is Boolean
    const val enableBarcodeFilterScript = "enableBarcodeFilterScript"

    /// The JS rule that will be used to filter barcodes.
    /// Value is String
    const val activeFilter = "activeFilter"

    /// Enable or Disable scan beep when a barcode is scanned.
    /// Value is Boolean
    const val enableScanBeep = "enableScanBeep"

    /// Enable or Disable hardware button trigger. If enabled, the center button will be the scan button. If disabled, you can control the scan engine via Intent.
    /// Value is Boolean
    const val enableHardwareButtonTrigger = "enableHardwareButtonTrigger"

    /// Enable or Disable keyboard output mode. When enabled, and a text field is in focus, the barcode will be injected into the text field.
    /// Value is Boolean
    const val enableKeyboardOutputMode = "enableKeyboardOutputMode"

    /// Enable or Disable insert return at the end of the scanned barcode.
    /// Value is Boolean
    const val enableInsertReturn = "enableInsertReturn"

    /// Enable or Disable overwrite the current content in the text field when a new barcode is scanned.
    /// Value is Boolean
    const val enableOverwriteCurrentText = "enableOverwriteCurrentText"

    /// Enable or Disable keys suggestion on the QWedge keyboard layout.
    /// When a key on the keyboard held down for a brief moment, a popup will be shown with suggested keys around the key that being held down.
    /// Value is Boolean
    const val enableKeysSuggestion = "enableKeysSuggestion"

    /// Enable or Disable intent output mode. This mode should be ON in order to receive the barcode via intents.
    /// Value is Boolean
    const val enableIntentOutputMode = "enableIntentOutputMode"

    /// The main intent API action. This action is used in the intent broadcast response to external app.
    /// By default it is set to "com.ipc.qwedge.intent.ACTION", but you can change it to other string that your app already filtering for.
    /// Value is String
    const val intentAction = "intentAction"

    /// The barcode data extra hold the intent extra key that contains the barcode data on intent broadcast.
    /// If your app is already has an extra key set for the barcode data, you can set this value to match with your app.
    /// Value is String
    const val barcodeDataExtra = "barcodeDataExtra"

    /// The barcode type extra hold the intent extra key that contains the barcode type on intent broadcast.
    /// If your app is already has an extra key set for the barcode type, you can set this value to match with your app.
    /// Value is String
    const val barcodeTypeExtra = "barcodeTypeExtra"

    /// The barcode type text extra hold the intent extra key that contains the barcode type text on intent broadcast.
    /// If your app is already has an extra key set for the barcode type text, you can set this value to match with your app.
    /// Value is String
    const val barcodeTypeTextExtra = "barcodeTypeTextExtra"

    /// The image extra that hold the image path in the activity result.
    /// Value is String
    const val imageExtra = "imageExtra"

    /// The settings parameters to turn on/off symbologies.
    /// Value is String
    const val parameters = "parameters"

    /// Enable or Disable intent broadcast response to the active app on foreground only. 
    /// When disabled, any app register to receive barcode data via intent will receive the broadcast even in background.
    /// Value is Boolean
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



## MagicFilters
With MagicFilters, you can write a JavaScript script to process the scanned barcode to determined if it should be accepted or rejected. You can check if the barcode contains any target characters or add prefix, suffix, or even return an entirely different barcode. If it is accepted, it would be sent to keyboard as keystroke, or broadcast via intent to all the apps that are setup to receive barcodes.

The returned barcode value can be a simple string, or even a JSON string.

### How to Create JavaScript File
You can write the JavaScript rules as normal as any other JavaScript files with a few important rules. Below is an example on how to create a JavaScript file (ModifyTestBarcode.js) that modify barcodes that match "Test" to a special barcode "12345" with symbology type 99. 

1. Creating The ModifyTestBarcode.js JavaScript File
JavaScript files are text files with the extension of .js and contain JavaScript code.

    ```JavaScript=

        // The content has a main function which will be called by QWedge 
        function ModifyTestBarcode(symbology, barcode) 
        {
            // Return the modified barcode
            if (barcode == "Test") {
                return { 
                    accept: true, 
                    adjBarcode: "12345", 
                    adjSymbology: "99", 
                    adjSymbologyText: "Code 99" };
            }

            // Return the barcode as is
            return {
                accept: true,
                adjBarcode: barcode
            }
        }
    
    ```

2. The main JavaScript function ModifyTestBarcode must take in 2 parameters:
    - Symbology (String)
    - Barcode (String)
    
3. The ModifyTestBarcode function must return an object with format as follow: 

    ```JavaScript=
        { 
            accept: Boolean, 
            adjBarcode: String,
            adjSymbology: customSymbology,          // Optional
            adjSymbologyText: customSymbologyText   // Optional
        }
    ```
    - `accept`: the value should be a boolean, that tell QWedge if the scanned barcode is accepted and passed the validation.
    - `adjBarcode`: the actual barcode value that should be broadcasted to your application, or a JSON string.
    - `adjSymbology`: the modified symbology type if you want to return a custom symbology, other than the original symbology from the engine.
    - `adjSymbologyText`: the modified symbology type text of your modified symbology above.
    
4. The JavaScript file name must match the main function name. 
    
    From the example above: the main function is called `ModifyTestBarcode`, so the JavaScript file name must be `ModifyTestBarcode.js`
    
5. JavaScript File Location

    The JavaScript file must be copied to this specified public location so QWedge can look for it: 

    ```
        ..Internal shared storage/Documents/QWedge/Scripts/
    ```

### Activate MagicFilters

To use MagicFilters, you need to enable `MagicFilters` either within the QWedge app or via `Intent`, and send a configuration to tell QWedge to use the JavaScript file that you just created.

Below is how you would enable `MagicFilters`, and set the active JavaScript rule that QWedge should use to process barcodes via configuration:

```kotlin=
    // Create a bundle that contains config values.
    var config = Bundle()
    // Enable Barcode Filter
    config.putBoolean("enableBarcodeFilterScript", true)
    // Set the active filter script
    config.putString("activeFilter", "ModifyTestBarcode")

    // Setup intent with config and send it.
    Intent().also {
        it.action = "com.ipc.qwedge.api.ACTION"
        it.putExtra("com.ipc.qwedge.api.SET_CONFIG", config)
        sendBroadcast(it)
    }
```


## QWedge Keyboard

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeyboard.png)

QWedge comes with a custom built keyboard. On the Halo device, QWedge keyboard is the default keyboard. On other devices, you need to turn it on via Languages & Input in Android Settings.


### Magnify Keys

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeyMagnify.png)

When `Keys Suggestion` is disbaled, tap & hold on the keyboard keys will display a magnified popup of that key. From there you can drag your finger across the keyboard, and let go on the key that you want to enter.


### Keys Suggestion Mode

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/AlphaKeySuggestion.png)

When `Keys Suggestion` is enabled, tap & hold down on a key will display a popup with suggested surrounding keys with bigger button size for easy to tap.


### Numeric Keyboard
#### Number Pad

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/NumericPadKeyboard.png)

The default numeric keyboard on the Halo device is the number pad for easy typing. To switch to number pad, just tap the `123` key.

#### Full Number & Symbol Keyboard

To switch to the full Number & Symbol keyboard, tap and hold the `123` key and select `?123` key

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/NumericKeyboardOptions.png)

The full Number & Symbol keyboard looks like below:

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/NumericFullKeyboard.png)


### Function Keyboard

QWedge also provides a Function keyboard. Tap the `Fn` key to activate Function keyboard

![](https://github.com/InfinitePeripherals/QWedge-Android/raw/main/assets/ringscanner/FunctionKeyboard.png)
