BarcodeScannerPlugin
====================

Cross platform Phonegap/Cordova Plugin of the Scandit Barcode Scanner SDK for iOS and Android

Follow the detailed instructions below to add a high-performance barcode scanner to your app in 5 min.

If you don't have a Phonegap app yet, we will show you how to generate a sample app below.  


Scandit Barcode Scanner SDK Plugin Integration via the Cordova Command Line Interface (CLI)
------------------------

The easiest way to install the Scandit Barcode Scanner plugin into your Phonegap/Cordova project is to use the [Cordova CLI](http://cordova.apache.org/docs/en/3.4.0/guide_cli_index.md.html#The%20Command-Line%20Interface).

* Install [Cordova CLI](http://cordova.apache.org/docs/en/3.4.0/guide_cli_index.md.html#The%20Command-Line%20Interface) if it is not already installed.
* [Sign up](http://www.scandit.com/pricing) and download the [Scandit Barcode Scanner SDK](http://www.scandit.com/barcode-scanner-sdk/) Cordova Plugins for iOS and Android from your Scandit account. Unzip the zip to a folder of your choice.

* Generate a sample Cordova project or use your existing Cordova project

To generate a sample project, use the following command line commands:
```
	cordova create helloworld
	cd hello world
	cordova platform add ios
	cordova platform add android
```

* Install the [Scandit Barcode Scanner](http://www.scandit.com/barcode-scanner-sdk/) Plugin using [Cordova CLI](http://docs.phonegap.com/en/3.1.0/guide_cli_index.md.html#The%20Command-line%20Interface)

```
        cordova plugin add  <path to downloaded,unzipped ScanditSDK Plugin for Phonegap/Cordova>
```

* Start using the Scandit Barcode Scanner SDK in your html code
    * Get the app key from your Scandit account
    * Invoke the Scandit Barcode Scanner by invoking the cordova.exec() function with the following parameters:

	`cordova.exec(function(success), function(cancel), "ScanditSDK", "scan", ["ENTER YOUR APP KEY HERE",{}]);`

    * See [Scandit Barcode Scanner SDK Documentation](http://docs.scandit.com) for the full API reference.


* Important(!!!):

    * if you decide against using the packaged zip with the Scandit Phonegap/Cordova plugin from the downloads page of your Scandit account and
      use the github src of the plugin instead (not recommended!), you will need to copy the libraries and resources from the native Scandit SDK
      builds for iOS and Android to the locations specified in the plugins.xml file.

    * note that there is a serious bug in Cordova 3.4.0 that does not allow you to build your app with Xcode 5.1/Cordova 3.4.0 on an iPhone 5S with an arm64 slice (http://shazronatadobe.wordpress.com/2014/03/12/xcode-5-1-and-cordova-ios/). A workaround until the Cordova bug fix release 3.4.1 will become available is to remove the "arm64" slice from the build settings in your Xcode project.


### Sample HTML + JS

```html
<!DOCTYPE html>
<html>
    <!--
     #
     # Licensed to the Apache Software Foundation (ASF) under one
     # or more contributor license agreements.  See the NOTICE file
     # distributed with this work for additional information
     # regarding copyright ownership.  The ASF licenses this file
     # to you under the Apache License, Version 2.0 (the
     # "License"); you may not use this file except in compliance
     # with the License.  You may obtain a copy of the License at
     #
     # http://www.apache.org/licenses/LICENSE-2.0
     #
     # Unless required by applicable law or agreed to in writing,
     # software distributed under the License is distributed on an
     # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     #  KIND, either express or implied.  See the License for the
     # specific language governing permissions and limitations
     # under the License.
     #
     -->
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name = "format-detection" content = "telephone=no"/>
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width;" />
        <link rel="stylesheet" type="text/css" href="css/index.css" />
        <title>Scandit SDK</title>
    </head>
    <body onload="onBodyLoad()" style="background: url(img/ScanditSDKDemo-Splash.png) no-repeat;background-size: 100%;background-color: #000000">
        <script type="text/javascript" src="cordova.js"></script>
        <script type="text/javascript" src="js/index.js"></script>
        <script type="text/javascript">
            function onBodyLoad()
            {
                document.addEventListener("deviceready", onDeviceReady, false);
            }

	    function success(resultArray) {

		alert("Scanned " + resultArray[0] + " code: " + resultArray[1]);

            	// NOTE: Scandit SDK Phonegap Plugin Versions 1.* for iOS report
            	// the scanning result as a concatenated string.
            	// Starting with version 2.0.0, the Scandit SDK Phonegap
            	// Plugin for iOS reports the result as an array
            	// identical to the way the Scandit SDK plugin for Android reports results.

            	// If you are running the Scandit SDK Phonegap Plugin Version 1.* for iOS,
            	// use the following approach to generate a result array from the string result returned:
            	// resultArray = result.split("|");
            }

            function failure(error) {
                alert("Failed: " + error);
            }

            function scan() {
                // See below for all available options.
                cordova.exec(success, failure, "ScanditSDK", "scan",
                             ["ENTER YOUR APP KEY HERE",
                              {"beep": true,
                              "1DScanning" : true,
                              "2DScanning" : true}]);
            }

            app.initialize();
            </script>

        <div align="center" valign="center">
            <input type="button" value="scan" onclick="scan()" style="margin-top: 230px; width: 100px; height: 30px; font-size: 1em"/>
        </div>
    </body>
</html>

```


Changelog
------------------------

**Scandit SDK Phonegap/Cordova Plugin for iOS and Android (4.1.0) - Oct 28st 2014**

 * upgraded to support Cordova 4.0+

**Scandit SDK Phonegap/Cordova Plugin for iOS and Android (4.1.0) - Aug 21st 2014**

 * upgraded to Scandit SDK for iOS 4.1.3 and Scandit SDK for Android 4.1.2 (see release notes in download section of your Scandit SDK for details)

    * [Release Notes of native Scandit SDK for iOS 4.1.3](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-ios_4.1.3)

    * [Release Notes of native Scandit SDK for Android 4.1.2](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-android_4.1.2)

 * added methods to enable/disable Codabar and Code93 barcode decoders (only available in enterprise editions and free trial)

**Scandit SDK Phonegap/Cordova Plugin for iOS and Android (4.0.1) - May 21st 2014**

 * upgraded to Scandit SDK for iOS 4.0.1 and Scandit SDK for Android 4.0.1 (see release notes in download section of your Scandit SDK for details)

    * [Release Notes of native Scandit SDK for iOS 4.0.1](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-ios_4.0.1)

    * [Release Notes of native Scandit SDK for Android 4.0.1](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-android_4.0.1)


**Scandit SDK Phonegap/Cordova Plugin for iOS and Android (4.0.0beta1) - March 31st 2014**

 * upgraded to Scandit SDK for iOS 4.0.0 and Scandit SDK for Android 4.0.0beta1 (see release notes in download section of your Scandit SDK for details)

    * [Release Notes of native Scandit SDK for iOS 4.0.0](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-ios_4.0.0)

    * [Release Notes of native Scandit SDK for Android 4.0.0beta1](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-android_4.0.0beta1)

 * fixed bug that prevented 'preferFrontCamera' parameter implementation from working properly.  


**Scandit SDK Phonegap/Cordova Plugin for iOS and Android (2.3.0) - November 26th 2013**

 * upgraded to Scandit SDK for iOS 3.2.1 and Scandit SDK for Android 3.5.2 (see release notes in download section of your Scandit SDK for details)

    * [Release Notes of native Scandit SDK for iOS 3.2.1](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-ios_3.2.1)

    * [Release Notes of native Scandit SDK for Android 3.5.2](https://ssl.scandit.com/account/sdk/release-notes/scanditsdk-community-ios_3.5.2)

 * updated documentation to use Cordova CLI instead of Plugman

 * added support for disableStandbyState (iOS only, under Android this parameter is ignored)

 * added parameter orientation that allows developers to restrict the orientation that are allowed for the scan UI.


**Scandit SDK Phonegap Plugin for iOS and Android (2.2.0) - September 30th 2013**

 * upgraded to Scandit SDK for iOS 3.1.1 and Scandit SDK for Android 3.5.1 (see release notes in download section of your Scandit SDK for details)


**Scandit SDK Phonegap Plugin for iOS (2.1.0) and Android (1.2.0) - August 6th 2013**

 * support for Phonegap/Cordova 3.0

 * upgraded to Scandit SDK 3.0.4 for iOS (for details see release notes in download section of your Scandit SDK account), Android version of plugin still uses Scandit SDK 3.3.1 for Android


**Scandit SDK Phonegap Plugin 2.0.1 for iOS only - June 17th 2013**

 * upgraded to Scandit SDK 3.0.3 for iOS which is a new bug fix release (see release notes of native iOS version for details)

**Scandit SDK Phonegap Plugin 2.0.0 for iOS only - May 11th 2013**

 * upgraded to new Scandit SDK 3.0.1 for iOS which comprises various new features: full screen scanning,
   improved autofocus management, better scan performance and robustness, new cleaner scan screen interface
   with the option to add a button to switch cameras and new symbologies (PDF417 beta and MSI-Plessey).

 * IMPORTANT: updated Scandit SDK Phonegap Plugin API to reflect updates in Scandit SDK 3.0.1 for iOS.
   This includes a number of new API options (see below), a number of options have also disappeared.

 * harmonized return results of Android and iPhone Plugin. In previous versions,
   the iOS Plugin would return a string, while the Android Plugin would return an array.
   Starting with Scandit SDK Phonegap Plugin for iOS 2.0.0, the iOS Plugin will also return an array.



**Scandit SDK Phonegap Plugin 1.1.0 for Android & iOS - April 2nd 2013**

 * upgraded to native Scandit SDK 2.2.7 for iOS and 3.3.1 for Android

 * includes support PLUGMAN

 * Fixed a bug that would freeze and stop the modal view from closing.

 * Scandit SDK Phonegap Plugin for Android now also supports barcode scanning in landscape mode.

 * Minor changes to ScanditSDKActivity.java




API for Scandit SDK Phonegap Plugin iOS and Android  
------------------------

See http://www.scandit.com/support for more information



License
-------
* This plug-in is released under the Apache 2.0 license: http://www.apache.org/licenses/LICENSE-2.0



Questions? Contact `info@scandit.com`.
