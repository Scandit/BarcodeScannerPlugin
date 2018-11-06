Use Scandit under the Telerik platform {#cordova-telerik}
=========================

Supported Features
----------------------------------------------------

The full functionality of the Scandit Phonegap plugin is available under Telerik when creating a Cordova hybrid app. However testing the picker using the simulator or the companion app is not possible.

Including the Scandit Barcode Scanner SDK plugin into your project
----------------------------------------------------

* Browser-based IDE: Open the "Properties" tab of your project (in the "Code" page), go to "Plugins" and chose "Import". Select the zip file downloaded from scandit.com
* CLI: Run
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
appbuilder plugin fetch <path-to-plugin>/scandit-barcodescanner-phonegap_VERSION.zip
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
in your project folder

Supported Cordova Versions
--------------------------------------------------

Targetting a cordova version <= 3.5 (Android 3.5.1) may result in a failure to build the scandit plugin.
