# Installation

## Preconditions
_Please make sure the latest [node](https://nodejs.org/en/) and [npm](https://www.npmjs.com/get-npm?utm_source=house&utm_medium=homepage&utm_campaign=free%20orgs&utm_term=Install%20npm) are installed._

To check if you have `node` and `npm` installed, run the following commands:

```bash
$ node -v
v8.6.0

$ npm -v
5.5.1
```

_Please note that your version numbers might differ._

## Install the Ionic CLI
Then, install the [Ionic CLI](https://ionicframework.com/docs/cli/) globally (you _may_ need `sudo`):

```bash
$ npm install -g ionic@latest
```

You can verify your installation with the `ionic --version` command.

```bash
$ ionic --version
3.19.0
```
_Example output, your version number might differ._

## Install Cordova

Ionic integrates with [Cordova](https://cordova.apache.org/) to provide native capabilities to your app.

```bash
$ npm install -g cordova
```

# Getting Started

## Install the dependencies

Run `npm install` to install the dependencies of the project that are needed to build it.

## Add the Scandit Cordova plugin

### Download

_If you don't have an account yet, you can sign up for a 30-day [Test SDK](https://ssl.scandit.com/customers/new?p=test)._

Login to your [Scandit SDK dashboard](https://ssl.scandit.com/sessions/new) and navigate to the [Downloads](https://ssl.scandit.com/sdk) page.

Find the Cordova/PhoneGap plugin of your choice (e.g. Barcode Scanner SDK for Phonegap) and download the latest version.

### Add the Scandit Cordova plugin

Unzip the plugin you downloaded in the previous step, then run the following command to add the plugin to the project:

```bash
$ ionic cordova plugin add <path to the unzipped plugin folder>
```

## Add the platform(s)

To add a platform to the project, run either or both of the following commands:

```bash
$ ionic cordova platform add ios
$ ionic cordova platform add android
```

_Note, that if you add the `ios` platform, you need to open the generated Xcode project and setup signing. After this is done, you can close Xcode and run the sample as explained below._

## Set the license key

Replace `-- ENTER YOUR SCANDIT LICENSE KEY HERE --` with your license key in `src/providers/scanner-service/scanner-service.ts`

## Run the sample on a device

```bash
ionic cordova run ios --device
```

Use the `--livereload`` option if you want to keep watching the source for changes and preview those on the device.  
_If you choose this option, please note that only the WebView contents are reloaded, which does not include native components, e.g. the barcode picker._

# App structure and logic

The purpose of the sample is to demonstrate using the Scandit SDK for Cordova inside an Ionic project.

The two main files of interest are `home.ts` and `scanner-service.ts`:
- `home.ts` is the only page used in the app and it shows a scanner as well as the scanned results.
- `scanner-service.ts` contains code related to interacting with the Scandit Cordova plugin, including the picker as well as handling the sizing of the barcode picker.

## Sizing
The barcode picker is shown as a platform native element and not an HTML element, so we need to be able to constrain it properly in the context of an Ionic page. As the page we're showing the scanner on includes a navigation bar, which has a size and position dependent on device and platform, we need to know it's size to position the picker correctly (right under the navigation bar). To achieve this, we're getting a reference to the Ionic content object in `home.ts` and as soon as the page is shown, we set it's `contentTop` as the scanner's `contentTop`. Keep in mind that `scanner` at this point is an instance of the `ScannerServiceProvider` that's wrapping the barcode picker.

When the `contentTop` is set for the scanner, the constraints for the picker are adjusted (`setScannerConstraints`), aligning it to the top and leaving some space between the bottom of the picker and the bottom of the screen. In addition, the height of the picker is also calculated (`contentHeight`), so that value can be used back on the home page to position the rest of the page relative to the picker, so it doesn't overlay any of the content.

## Scanning

The home page (`home.ts`) is where the picker and the results are shown and the scanning can be resumed, as well as the continuous mode can be turned on and off.

As soon as the page is shown, the scanner is set up: the delegate is specified and scanning is started. The delegate is necessary in this app to get notified of new scan results, this could potentially be achieved in different ways and is specific to this app only and not the SDK.

The picker is set up when the `ScannerServiceProvider` instance is created, this is when the license key is set and the specific settings are defined and passed to the picker that the `ScannerServiceProvider` is wrapping.  
As continuous mode is handled by the app itself, we set the picker to be always in continuous mode and later take care of pausing ourselves if necessary.

When the scanner is started, the picker is shown and scanning is started. We also specify calling the delegate if available when a barcode is scanned.

If continuous mode is disabled on the home page, scanning is paused as soon as something is scanned and is resumed if the user taps a button, which triggers calling the `resumeScanning` function (`home.ts`), which resumes the scanner and any results that the page was keeping track of.
