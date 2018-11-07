Change the Length of the Barcodes to Decode {#cordova-active-symbols-counts}
=========================================================================

## Motivation

By default the Scandit Barcode Scanner scans barcodes with a fixed symbol count range.
Under certain conditions it is helpful to modify the length of the barcode to be decoded, for example if

* you want to scan longer codes which cannot be decoded with the default settings.
* to optimize decoder performance for codes of certain lengths when the application only requires scanning particular barcode lengths.

The active symbol count setting is ignored for fixed-size barcodes (EAN and UPC barcodes) as well as 2d codes.


## Change the barcode length

To know the symbol count of a barcode, you can either:
* scan it with the "Any Code" mode of the Scandit demo app, available on the Apple and Google Play Store.
* have a look at <a href="../c_api/symbologies.html">Calculating symbol counts for variable-length symbologies</a>.

Changing the length of the barcodes is done through {@link Scandit.SymbologySettings.activeSymbolCounts activeSymbolCounts} on the symbology settings object.
Following an example of changing for Code 128 barcodes the symbol count range (7-20).


~~~~~~~~~~~~~~~~{.java}

Var symbolcounts = [7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20];
settings.getSymbologySettings(Scandit.Barcode.Symbology.CODE128).activeSymbolCounts = symbolcounts;

~~~~~~~~~~~~~~~~

Available since SDK version 4.11.0.
