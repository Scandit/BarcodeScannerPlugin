Scan multiple barcodes at once with MatrixScan {#cordova-multiple-scanning}
===================================================

## Motivation

Sometimes you have packages/pallets with several codes, with the Scandit Barcode Scanner you can scan all the codes or just a subset at the same time, saving time and money. To do this you have to use the Matrix Scan option.


## Matrix Scan

Matrix Scan constantly tracks recognized barcodes' position. Thanks to this approach it is possible to implement applications which would not be feasible with the traditional barcode scanner.

### Enabling Matrix Scan

To implement Matrix Scanning:

- Enable matrix scanning in the scan settings by setting {@link Scandit.ScanSettings.matrixScanEnabled} to `true`.

- Set the maximum number of codes to be decoded every frame through {@link Scandit.ScanSettings.maxNumberOfCodesPerFrame} to something higher than 1, depending on the environment of the codes it is a good idea to set it higher than the number of codes that you actually want to scan.

- Change the GUI style through {@link Scandit.ScanOverlay.setGuiStyle} to {@link  Scandit.ScanOverlay.GuiStyle Scandit.ScanOverlay.GuiStyle.MATRIXSCAN}.

- For performance reasons it is important not to implement `didScan` callback while using MatrixScan in order to avoid unnecessary callback invocations. All the information about detected codes should be retrieved using `didRecognizeNewCodes` callback.

- Pass a `didRecognizeNewCodes` callback as a {@link Scandit.BarcodePicker.show} function argument

- In the `didRecognizeNewCodes` callback you can get currently tracked barcodes from the session through {@link Scandit.MatrixScanSession.newlyTrackedCodes}

At this point you can start the barcode picker and any recognized barcodes of the enabled symbologies will be highlighted by a filled green rectangle. Barcodes that have been localized but not recognized will be highlighted by a green border.

~~~~~~~~~~~~~~~~{.java}
function scan() {
    //...

    // The scanning behavior of the barcode picker is configured through scan
    // settings. We start with empty scan settings and enable a generous set
    // of 1D symbologies.
    // In your own apps, only enable the symbologies you actually need.
    var settings = new Scandit.ScanSettings();
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN13, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN8, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPCA, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPCE, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.CODE39, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.CODE128, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.ITF, true);

    // Enable matrix scan and set the max number of barcodes that can be recognized per frame
    // to some reasonable number for your use case. The max number of codes per frame does not
    // limit the number of codes that can be tracked at the same time, it only limits the
    // number of codes that can be newly recognized per frame.
    settings.matrixScanEnabled = true;
    settings.maxNumberOfCodesPerFrame = 10;

    picker = new Scandit.BarcodePicker(settings);

    // Set continuous mode to not return after the first scan.
    picker.continuousMode = true;

    // Set the GUI style to MATRIXSCAN in order to use the default code visualization.
    picker.getOverlayView().setGuiStyle(Scandit.ScanOverlay.GuiStyle.MATRIXSCAN);

    // When matrix scan is enabled beeping/vibrating is often not wanted.
    picker.getOverlayView().setBeepEnabled(false);
    picker.getOverlayView().setVibrateEnabled(false);

    // In the next paragraphs of this article you will find a few use cases with
    // example implementation of didRecognizeNewCodes callback.
    picker.show(null, null, null, null, didRecognizeNewCodes);
    picker.startScanning();
}
~~~~~~~~~~~~~~~~

### Wait for multiple codes to be scanned

One of the use cases of Matrix Scan is to detect when the specified number of expected codes has been decoded by the scanner. To implement this scenario:

- In the `didRecognizeNewCodes` callback, wait until the number of expected codes have been decoded, then store the codes and pause/stop the session. If you pause and plan to resume but want to start a new session, make sure that you clear the session first.

~~~~~~~~~~~~~~~~{.java}

var detectedCodes = [];

function didRecognizeNewCodes(matrixScanSession) {
  // Number of expected barcodes.
  var numExpectedCodes = 3;
  // Get all the scanned barcodes from the session.
  detectedCode.push(matrixScanSession.newlyTrackedCodes);
  // If the number of scanned codes is greater than or equal to the number of expected barcodes,
  // pause the scanning and clear the session (to remove recognized barcodes).
  if (detectedCodes.length >= numExpectedCodes) {
  // Stop scanning or pause and clear the session.
      scanSession.stopScanning();
      // ...
  }
}

function scan() {
    //...
    // After configuring your barcodePicker you can pass didRecognizeNewCodes callback as shown below:
    picker.show(null, null, null, null, didRecognizeNewCodes);
    picker.startScanning();
}
~~~~~~~~~~~~~~~~

### Rejecting unwanted codes

Like normal scanning Matrix Scanning provides you the option to reject codes. Just like for normal scanning rejected codes are not added to the scan session and do not provide any feedback like vibrating or beeping. In Matrix Scan the rejected codes are still drawn on the screen but in a different color. This gives the user visual feedback that a certain code is not the one you are looking for. To implement rejection:

- Pass a `didRecognizeNewCodes` callback as a {@link Scandit.BarcodePicker.show} function argument

- In the `didRecognizeNewCodes` callback you can get currently tracked barcodes from the session through {@link Scandit.MatrixScanSession.newlyTrackedCodes}

- Once you've checked the tracked codes for their symbologies and/or data you can reject them through {@link Scandit.MatrixScanSession.rejectTrackedCode}.

**Note:** Rejecting in the `didScan` callback is not allowed when using Matrix Scan.

~~~~~~~~~~~~~~~~{.java}

function didRecognizeNewCodes(matrixScanSession) {
    // As an example, the following code will reject all EAN8 codes:
    matrixScanSession.newlyTrackedCodes.forEach(element => {
        if (element.symbology == Scandit.Barcode.Symbology.EAN8) {
            matrixScanSession.rejectTrackedCode(element)
        }
    });
}

function scan() {
    //...
    // After configuring your barcodePicker you can pass didRecognizeNewCodes callback as shown below:
    picker.show(null, null, null, null, didRecognizeNewCodes);
    picker.startScanning();
}

~~~~~~~~~~~~~~~~

### Customizing barcodes highlighting color

You can always customize the color in which detected barcodes are highlighted on the screen. To do this you can use {@link Scandit.ScanOverlay.setMatrixScanHighlightingColor} function. You can set color for 3 states of the detected codes:
- {@link Scandit.ScanOverlay.MatrixScanState LOCALIZED} - displayed when the code has been localized but not yet recognized
- {@link Scandit.ScanOverlay.MatrixScanState RECOGNIZED} - displayed when the code has been recognized
- {@link Scandit.ScanOverlay.MatrixScanState REJECTED} - displayed when the code has been visually rejected

For example applying this setting will highlight all recognized barcodes using `yourColor`:

~~~~~~~~~~~~~~~~{.java}

function scan() {
    //...
    picker.getOverlayView().setMatrixScanHighlightingColor(Scandit.ScanOverlay.MatrixScanState.RECOGNIZED, /*yourColor*/)
    //...
    picker.startScanning();
}

~~~~~~~~~~~~~~~~
