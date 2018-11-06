
Define the scanning area  {#cordova-restrict-scanning-area}
=========================================================================

By default the Scandit Barcode Scanner searches the entire camera feed for barcodes. Under certain conditions it is helpful to restrict the area in which barcodes are recognized, for example if

* you only show a part of the barcode picker, codes that are not visible should generally not be recognized.

* there are multiple barcodes close together and the user has to be able to select a specific one.


## Restrict the scanning area

Restricting the scanning area is done through {@link Scandit.ScanSettings.activeScanningAreaPortrait activeScanningAreaPortrait} and {@link Scandit.ScanSettings.activeScanningAreaLandscape activeScanningAreaLandscape} on the settings object. The two properties allow you to have different scanning areas dependent on the device orientation. The area itself is specified as a rectangle with coordinates relative to the camera preview's size where the top-left corner is 0/0 and the bottom-right corner is 1/1. Setting Scandit.Rect(0.0, 0.0, 1.0, 1.0) would set the scanning area to the entire camera preview. Following an example of restricting the scanning area to 100% of the camera preview width and 10% of the camera preview height centered around the middle of the camera preview.

~~~~~~~~~~~~~~~~{.java}

var settings = Scandit.ScanSettings();
settings.activeScanningAreaPortrait = new Scandit.Rect(0.0, 0.45, 1.0, 0.1);

~~~~~~~~~~~~~~~~

Restricting the scanning area to only the bottom half of the camera preview would look the following way:

~~~~~~~~~~~~~~~~{.java}

settings.activeScanningAreaPortrait = new Scandit.Rect(0.0, 0.5, 1.0, 0.5);

~~~~~~~~~~~~~~~~
<br/>


## Adjust the viewfinder to match the scanning area

If you reduced the size of the scanning area you likely also want to adjust the size of the viewfinder that guides the user. You can do this by setting the viewfinder dimension on the {@link Scandit.ScanOverlay ScanOverlay}. The coordinate system here is relative to the {@link Scandit.BarcodePicker BarcodePicker}'s size.

~~~~~~~~~~~~~~~~{.java}

picker.getOverlayView().setViewfinderDimension(0.6, 0.1, 0.6, 0.1);

~~~~~~~~~~~~~~~~

Alternatively you can also use the new laser GUI style. This style is mostly for cases where the scanning area is restricted to a relatively low height (around 10% to 20%) as it guides the user to place the code along the laser line.

~~~~~~~~~~~~~~~~{.java}

picker.getOverlayView().setGuiStyle(Scandit.ScanOverlay.GuiStyle.LASER);

~~~~~~~~~~~~~~~~

<br/>
