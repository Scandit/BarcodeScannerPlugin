Run the samples {#cordova-examples}
===================================

The Scandit Barcode Scanner SDK (downloadable from https://ssl.scandit.com/sdk) comes with three demos:
* Simple: shows how to use the barcode scanner in a cordova project.
* Extended: allows the user to configure the barcode scanner as well as the UI from the application itself.
* MatrixScan: shows how to use MatrixScan.

You will also find additional samples on our <a href="https://github.com/Scandit/barcodescanner-sdk-cordova/tree/master">GitHub repository</a>, in particular the ionic-sample, which shows how to use the barcode scanner in an ionic project.

For barcode scanner usage examples, you can either use one of the sample apps included in the plugin archive, or paste one of the samples below into your www/index.html file.

## Build the sample apps

In order to build the sample apps you must import one of them upon creation of your project as follows.

Note that you will still need to add the platform and plugin as described in {@link cordova-integrate here}. You will also need to replace the license key in the samples with your license key.



### Download the Scandit Barcode Scanner

Download the plugin from your account on https://ssl.scandit.com/sdk. You can sign-up for a free trial if you do not have an account yet. Unzip the plugin.

### Set license key

The samples are inside the sample folder of the SDK zip file. Unzip the SDK zip file (or the repository downloaded from the <a href="https://github.com/Scandit/barcodescanner-sdk-cordova/tree/master">GitHub repository</a>) and set your license key in the following files:
* Simple Sample: <path-to-plugin>/samples/simple/www/index.html
* Extended Sample: <path-to-plugin>/samples/extended/src/providers/scanner.ts
* MatrixScan Sample: <path-to-plugin>/samples/matrixscan/www/index.html
* OCR Sample: <path-to-repository>/samples/ocr/src/providers/scanner.ts
* Ionic Sample: <path-to-repository>/samples/ionic-sample/src/providers/scanner-service/scanner.ts


### Build the sample

#### Build the Simple or MatrixScan Sample

Go to the directory that should contain your project.
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cd <path-to-folder-to-contain-cordova-project>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Create your app from the plugin's template (do not use the directory, use the plugin from https://ssl.scandit.com/sdk).
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cordova create helloworld --id "com.scandit.helloworld" --template <path-to-plugin>/samples/<name of the sample>/www/
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#### Build the Extended or OCR sample

Run `npm run build` from the sample's folder for the build files in the `www` folder to be updated.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cd <path-to-repository>/samples/<name of the sample>
> npm install
> npm run build
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Go to the folder directory that should contain your project.
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cd <path-to-folder-to-contain-cordova-project>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Create your app from the plugin's template (do not use the directory, use the plugin from https://ssl.scandit.com/sdk).
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cordova create helloworld --id "com.scandit.helloworld" --link-to <path-to-plugin>/samples/<name of the sample>/www/
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Note that the `--link-to` option will symlink to the specified www directory without creating a copy. For more information, see https://cordova.apache.org/docs/en/latest/reference/cordova-cli/#cordova-create-command.

#### Build the ionic sample

Run `npm install` from the sample's folder.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cd <path-to-repository>/samples/ionic-sample
> npm install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Add the platform(s) and the plugins

Make sure to use the plugin from https://ssl.scandit.com/sdk (not the one from the GitHub directory).

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cordova platform add <ios or android>
> cordova plugin add <path to downloaded and unzipped plugin>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Note: add `ionic` in front of the commands if you are using ionic.

### Run the sample

* On iOS, open the project under "project folder"/platforms/ios in XCode and run it.
* On Android, run the following:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~{.java}
> cordova run android --device
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
