Configure OCR (text recognition) {#cordova-config-ocr}
===================================

## Motivation

OCR enables text recognition for your mobile scanning apps. This means you can add text recognition to your organization’s mobile data capture workflows. As a result, you can seamlessly switch between reading barcodes and text with the touch of a button. An OCR sample is shipped in the zip file of our OCR SDK.

## License Key

A dedicated OCR License key and SDK should be used. Please contact us (https://support.scandit.com/hc/en-us/requests/new) for more details.

## Implementing OCR
To integrate the OCR into your Cordova app, follow the simple steps below:

#### Add callbacks to handle the scanning event

You now need to define the functions that are referenced in the show() call. All functions take one argument.

~~~~~~~~~~~~~~~~{.java}

	function success(session) {
		alert("Scanned text: " + session.text);

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

#### Set the text scan settings

- Create a Scan Settings and Text Recognition Settings.
- Set the recognition mode to “Text”.
- Specify the format/structure of the text to be scanned: regular expression which matches your text and white list of recognizable characters.
- Set the area in which text is to be recognized.

~~~~~~~~~~~~~~~~{.java}
let settings = new Scandit.ScanSettings();
settings.recognitionMode = Scandit.ScanSettings.RecognitionMode.TEXT;
settings.textRecognition = new Scandit.TextRecognitionSettings();

settings.textRecognition.regex = '([A-Z]{2}[0-9]{2}\\s([0-9]{4}\\s){4}([A-Z0-9]{1}))';
settings.textRecognition.characterWhitelist = "ABC123456789";

settings.activeScanningAreaPortrait = new Scandit.Rect(0.05, 0.45, 0.9, 0.1); // default active scanning area
settings.textRecognition.areaPortrait = settings.activeScanningAreaPortrait;
~~~~~~~~~~~~~~~~

#### Instantiate the barcode picker

Please refer to \ref cordova-integrate.
