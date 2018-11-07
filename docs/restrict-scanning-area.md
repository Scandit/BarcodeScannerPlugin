
Define the scanning area  {#cordova-restrict-scanning-area}
=========================================================================

By default the Scandit Barcode Scanner searches the entire camera feed for barcodes. Under certain conditions it is helpful to restrict the area in which barcodes are recognized, for example if

* you only show a part of the barcode picker, codes that are not visible should generally not be recognized.

* there are multiple barcodes close together and the user has to be able to select a specific one.


## Restrict the scanning area

Restricting the scanning area is done through {@link Scandit.ScanSettings.activeScanningAreaPortrait activeScanningAreaPortrait} and {@link Scandit.ScanSettings.activeScanningAreaLandscape activeScanningAreaLandscape} on the settings object. The two properties allow you to have different scanning areas dependent on the device orientation. The area itself is specified as a rectangle with coordinates relative to the camera preview's size where the top-left corner is 0/0 and the bottom-right corner is 1/1. Setting `Scandit.Rect(left:0.0, top:0.0, width:1.0, height:1.0)` would set the scanning area to the entire camera preview.

<br/>
![Coordinates](img/cordova/coordinate-system.png)
<br/>

The following sets the portrait's scanning area to 90% of the camera preview width and 10% of the preview height (centered).

~~~~~~~~~~~~~~~~{.java}

var settings = Scandit.ScanSettings();
settings.activeScanningAreaPortrait = new Scandit.Rect(0.05, 0.45, 0.9, 0.1);

~~~~~~~~~~~~~~~~

## Adjust the viewfinder to match the scanning area

The viewFinder is the white rectangle that helps the user to aim for the barcode. If you restricted the scanning area, you most likely want to adjust the viewFinder's size accordingly. You can do this by setting the viewfinder dimension on the {@link Scandit.ScanOverlay ScanOverlay}. The coordinate system here is relative to the {@link Scandit.BarcodePicker BarcodePicker}'s size.

To adjust the viewFinder's size to the previous example (90% width, 10% height):

~~~~~~~~~~~~~~~~{.java}

picker.getOverlayView().setViewfinderDimension(0.9, 0.1, 0.9, 0.1);

~~~~~~~~~~~~~~~~

Alternatively, you can use the laser GUI style. It will display a laser line instead of the white rectangle. This style is useful when you want to scan in a small horizontal strip centered on the scanline in the center of the preview as it guides the user to place the code along the laser line.

~~~~~~~~~~~~~~~~{.java}

picker.getOverlayView().setGuiStyle(Scandit.ScanOverlay.GuiStyle.LASER);

~~~~~~~~~~~~~~~~

<br/>
