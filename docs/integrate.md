Integrate the Scandit Barcode Scanner plugin into your app     {#cordova-integrate}
===================================

To integrate the Scandit Barcode Scanner into your Cordova app, follow the simple steps below.
 
## Get the Scandit Barcode Scanner SDK

Choose a plan (e.g., “Consumer Apps”, "Professional Apps", or “Enterprise/OEM” plan) at http://www.scandit.com/pricing and download the Scandit Barcode Scanner SDK for Android from your account.
<br/>
![Download page](img/cordova/DownloadPage.png)
<br/>


## Create a new project

If you do not have a Cordova project yet, you should create a new one.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
    cordova create helloworld --id "com.scandit.helloworld"
    cd helloworld
    cordova platform add ios
    cordova platform add android
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## Add the plugin to your project

Use the cordova CLI to add the plugin to your already existing project.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
    cd <directory of your project>
	cordova plugin add <path to downloaded and unzipped plugin>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


## Instantiate and configure the barcode picker

The scanning process is managed by the {@link Scandit.BarcodePicker BarcodePicker}. Before instantiating the picker, you will have to set your Scandit Barcode Scanner license key. The key is available from your Scandit Barcode Scanner SDK account at http://account.scandit.com in the License Keys section. The barcode scanning is configured through an instance of scan settings that you pass to the BarcodePicker constructor. 

~~~~~~~~~~~~~~~~{.java}

// Set your license key.
Scandit.License.setAppKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --");

var settings = new Scandit.ScanSettings();
settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN13, true);
settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPC12, true);
settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN8, true);

// Instantiate the barcode picker by using the settings defined above.
var picker = new Scandit.BarcodePicker(settings);

~~~~~~~~~~~~~~~~


## Show the scan UI

Show the scanner to the user through {@link Scandit.BarcodePicker.show(success, manual, failure) show(success, manual, failure)}. You can pass it three callback functions, one for when a barcode is recognized, one for when a code was manually entered and one for when the scan process was canceled by the user.

~~~~~~~~~~~~~~~~{.java}

picker.show(success, null, failure);

~~~~~~~~~~~~~~~~

For more information on the different ways to add the barcode picker to your view hierarchy, consult \ref android-scanview-options.


## Add callbacks to handle the scanning event 

You now need to define the functions that are referenced in the show() call. All functions take one argument, the manual 

~~~~~~~~~~~~~~~~{.java}

	function success(session) {
		alert("Scanned " + session.newlyRecognizedCodes[0].symbology + " code: " + session.newlyRecognizedCodes[0].data);
		
		// If you are using continuous scanning you might want to stop here. Please note that 
		// you will have to use session.stopScanning()/session.pauseScanning() instead of the 
		// corresponding method on the picker. This will avoid a race condition and immediately stop 
		// the scanning process after the success callback has finished executing.
		session.stopScanning();
	}
	
	function manual(content) {
		alert("Manual: " + content);
	}
	
	function failure(error) {
		alert("Failed: " + error);
	}

~~~~~~~~~~~~~~~~


## Start the scanner 

Start the actual scanning process to start the camera and look for codes.

~~~~~~~~~~~~~~~~{.java}

picker.startScanning();

~~~~~~~~~~~~~~~~

<br/>

## Build and run the app

Compile your project. Attach a device and run the app on your desired plattform.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
    cordova build
    cordova run android
    cordova run ios
    cordova run windows
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## Next steps

* \ref cordova-examples
* \ref cordova-cropped-or-scaled-picker
* \ref cordova-restrict-scanning-area
