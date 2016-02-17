Example code     {#cordova-examples}
===================================

## Build the sample apps

In order to build the simple or extended sample apps you must import one of them upon creation of your project as follows:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
    phonegap create helloworld --copy-from <path-to-repository>/samples/simple/www/
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

or

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
    phonegap create helloworld --copy-from <path-to-repository>/samples/extended/www/
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Note that you will still need to add the platform and plugin as described in {@link cordova-integrate here}.


## Simple Fullscreen

This shows the simplest way of using the plugin. The scanner is opened full screen and is closed as soon as a barcode is scanned, returning the result to a function specified by you.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}

<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="format-detection" content="telephone=no" />
        <meta name="msapplication-tap-highlight" content="no" />
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, 
        	minimum-scale=1, width=device-width, height=device-height" />
        <link rel="stylesheet" type="text/css" href="css/index.css" />
        <title>Scandit Barcode Scanner</title>
    </head>
    <body>
        <script type="text/javascript" src="cordova.js"></script>
        <script type="text/javascript">

            // On Windows, the alert function doesn't exist, so we add it.
            window.alert = window.alert !== undefined ? window.alert : function (message) {
                var alertBox = new Windows.UI.Popups.MessageDialog(message);
                alertBox.showAsync();
            };
            function success(session) {
				alert("Scanned " + session.newlyRecognizedCodes[0].symbology 
						+ " code: " + session.newlyRecognizedCodes[0].data);
            }

            function failure(error) {
                alert("Failed: " + error);
            }

            function scan() {
				Scandit.License.setAppKey("-- ENTER YOUR APP KEY HERE --");
				var settings = new Scandit.ScanSettings();
				settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN13, true);
				settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPC12, true);
				settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN8, true);
			
				var picker = new Scandit.BarcodePicker(settings);
				picker.show(success, null, failure);
				picker.startScanning();
            }
        </script>

        <div align="center" valign="center">
            <input type="button" value="scan" onclick="scan()" style="margin-top: 230px; 
            		width: 100px; height: 30px; font-size: 1em"/>
        </div>
    </body>
</html>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

<br/>


## Continuous Scaled/Cropped

This example demonstrates how to display the scanner as an overlay on top of your webview. It features four buttons. The first pair of buttons show and close the picker. The second pair stops and restarts the scanner while keeping it visible, they also change the margins and viewfinder size.

This example also uses the {@link Scandit.BarcodePicker.continousMode continuousMode} which lets you scan multiple barcodes in sequence without having to reopen the picker each time.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <meta name="format-detection" content="telephone=no" />
        <meta name="msapplication-tap-highlight" content="no" />
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, 
        	minimum-scale=1, width=device-width, height=device-height" />
        <link rel="stylesheet" type="text/css" href="css/index.css" />
        <title>Scandit Barcode Scanner</title>
    </head>
    <body>
        <script type="text/javascript" src="cordova.js"></script>
        <script type="text/javascript">
			
            // On Windows, the alert function doesn't exist, so we add it.
            window.alert = window.alert !== undefined ? window.alert : function (message) {
                var alertBox = new Windows.UI.Popups.MessageDialog(message);
                alertBox.showAsync();
            };

			var picker;
			
            function success(session) {
				alert("Scanned " + session.newlyRecognizedCodes[0].symbology 
						+ " code: " + session.newlyRecognizedCodes[0].data);
            }

            function failure(error) {
                alert("Failed: " + error);
            }
            
            function scan() {
				Scandit.License.setAppKey("-- ENTER YOUR APP KEY HERE --");
				var settings = new Scandit.ScanSettings();
				settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN13, true);
				settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPC12, true);
				settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN8, true);
				settings.codeDuplicateFilter = 3000;
			
				picker = new Scandit.BarcodePicker(settings);
				picker.continuousMode = true;
				picker.getOverlayView().setViewfinderDimension(0.9, 0.2, 0.6, 0.2);
			
				picker.setMargins(new Scandit.Margins(0, 0, 0, 200), null, 0);
				picker.show(success, null, failure);
				picker.startScanning();
            }

            function stop() {
            	picker.stopScanning();
            	picker.setMargins(new Scandit.Margins(0, 0, 0, 400), null, 0.5);
				picker.getOverlayView().setViewfinderDimension(0.9, 0.1, 0.6, 0.1);
            }

            function start() {
            	picker.startScanning();
            	picker.setMargins(new Scandit.Margins(0, 0, 0, 200), null, 0.5);
				picker.getOverlayView().setViewfinderDimension(0.9, 0.2, 0.6, 0.2);
            }

            function cancel() {
            	picker.cancel();
            }
        </script>

        <div>
            <input type="button" value="scan" onclick="scan()" style="position: absolute; 
            		bottom: 80px; left: 15%; width: 30%; height: 30px; font-size: 1em"/>
            <input type="button" value="cancel" onclick="cancel()" style="position: absolute;
            		bottom: 80px; right: 15%; width: 30%; height: 30px; font-size: 1em"/>
            <input type="button" value="stop" onclick="stop()" style="position: absolute; 
            		bottom: 30px; left: 15%; width: 30%; height: 30px; font-size: 1em"/>
            <input type="button" value="restart" onclick="start()" style="position: absolute; 
            		bottom: 30px; right: 15%; width: 30%; height: 30px; font-size: 1em"/>
        </div>
    </body>
</html>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

## Next steps

* \ref cordova-cropped-or-scaled-picker
* \ref cordova-restrict-scanning-area

