const barcode = new Scandit.Barcode();
barcode.isGs1DataCarrier();
barcode.isRecognized();
console.log(barcode.data, barcode.rawData, barcode.symbology, barcode.compositeFlag);

const symbology = Scandit.Barcode.Symbology.CODE39;

const callbacks = new Scandit.Callbacks();
callbacks.didScan = console.log;
callbacks.didManualSearch = console.log;
callbacks.didCancel = console.log;
callbacks.didRecognizeText = recognizedText => console.log(recognizedText.text);
callbacks.didRecognizeNewCodes = console.log;
callbacks.didChangeState = console.log;
callbacks.didChangeProperty = console.log;

const callbacks2: Scandit.Callbacks = {
    didScan: console.log,
};

let constraints = new Scandit.Constraints();
constraints.topMargin = 12;
constraints.width = 1;
constraints.height = 0.3;

Scandit.License.setAppKey("foobarkey");

const margins = new Scandit.Margins(10, 10, 10, 10);
const marginsPercentage = new Scandit.Margins('10%', '10%', '10%', '10%');

const matrixScanSession = new Scandit.MatrixScanSession([barcode]);
matrixScanSession.rejectTrackedCode(matrixScanSession.newlyTrackedCodes[0]);

const point = new Scandit.Point(10, 10);
console.log(point.x, point.y);

const quad = new Scandit.Quadrilateral(point, point, point, point);
console.log(quad.bottomLeft, quad.bottomRight, quad.topLeft, quad.topRight);

const bp = new Scandit.BarcodePicker(new Scandit.ScanSettings());
const scanSession = new Scandit.ScanSession([barcode], [barcode], [barcode], bp);
scanSession.pauseScanning();
scanSession.stopScanning();
scanSession.rejectCode(scanSession.allRecognizedCodes[0]);
scanSession.rejectCode(scanSession.newlyLocalizedCodes[0]);
scanSession.rejectCode(scanSession.newlyRecognizedCodes[0]);

const textRecognitionSettings = new Scandit.TextRecognitionSettings();
textRecognitionSettings.areaPortrait = new Scandit.Rect(1, 1, 1, 1);
textRecognitionSettings.regex = 'foo';
textRecognitionSettings.characterWhitelist = 'abc';

const symbologySettings = new Scandit.SymbologySettings();
symbologySettings.enabled = true;
symbologySettings.colorInvertedEnabled = false;
symbologySettings.extensions = [Scandit.SymbologySettings.Extension.TINY];
symbologySettings.checksums = [Scandit.SymbologySettings.Checksum.MOD_10];
symbologySettings.activeSymbolCounts = [1, 2];

const barcodePicker = new Scandit.BarcodePicker(new Scandit.ScanSettings());
barcodePicker.show(console.log, console.log, undefined, undefined, console.log);
barcodePicker.show(callbacks);
barcodePicker.cancel();
barcodePicker.applyScanSettings(new Scandit.ScanSettings());
barcodePicker.setConstraints(constraints, constraints, 1);
barcodePicker.setOrientations([Scandit.BarcodePicker.Orientation.LANDSCAPE_LEFT]);
barcodePicker.setMargins(margins, margins, 1);
barcodePicker.pauseScanning();
barcodePicker.resumeScanning();
barcodePicker.stopScanning();
barcodePicker.startScanning();
barcodePicker.startScanning();
barcodePicker.switchTorchOn(true);
barcodePicker.getOverlayView();
barcodePicker.convertPointToPickerCoordinates(point);

const overlay = barcodePicker.getOverlayView();
overlay.setGuiStyle(Scandit.ScanOverlay.GuiStyle.DEFAULT);
overlay.setBeepEnabled(false);
overlay.setVibrateEnabled(false);
overlay.setTorchEnabled(false);
overlay.setTorchButtonMarginsAndSize(1, 1, 1, 1);
overlay.setTorchButtonOffAccessibility('foo', 'foo');
overlay.setTorchButtonOnAccessibility('foo', 'foo');
overlay.setCameraSwitchVisibility(Scandit.ScanOverlay.CameraSwitchVisibility.ALWAYS);
overlay.setCameraSwitchButtonMarginsAndSize(1, 1, 1, 1);
overlay.setCameraSwitchButtonBackAccessibility('foo', 'foo');
overlay.setCameraSwitchButtonFrontAccessibility('foo', 'foo');
overlay.setViewfinderDimension(1, 1, 1, 1);
overlay.setViewfinderColor('foo');
overlay.setViewfinderDecodedColor('foo');
overlay.showSearchBar(false);
overlay.setSearchBarActionButtonCaption('foo');
overlay.setSearchBarCancelButtonCaption('foo');
overlay.setSearchBarPlaceholderText('foo');
overlay.setMinSearchBarBarcodeLength(1);
overlay.setMaxSearchBarBarcodeLength(1);
overlay.setToolBarButtonCaption('foo');
overlay.setProperty('foo', 'bar');
overlay.setTextRecognitionSwitchVisible(false);
overlay.setMissingCameraPermissionInfoText('foo');
overlay.updateOverlayIfExists();
overlay.setMatrixScanHighlightingColor(Scandit.ScanOverlay.MatrixScanState.LOCALIZED, 'foo');

const settings = new Scandit.ScanSettings();
settings.getSymbologySettings(Scandit.Barcode.Symbology.CODE39);
settings.setSymbologyEnabled(Scandit.Barcode.Symbology.CODE39, true);
settings.setProperty('foo', 'bar');
settings.recognitionMode = Scandit.ScanSettings.RecognitionMode.CODE;
settings.highDensityModeEnabled = false;
settings.activeScanningAreaPortrait = new Scandit.Rect(1, 1, 1, 1, );
settings.activeScanningAreaLandscape = new Scandit.Rect(1, 1, 1, 1, );
settings.deviceName = 'foo';
settings.matrixScanEnabled = true;
settings.cameraFacingPreference = Scandit.ScanSettings.CameraFacing.FRONT;
settings.scanningHotSpot = new Scandit.Point(1, 1);
settings.relativeZoom = 1;
settings.maxNumberOfCodesPerFrame = 5;
settings.codeRejectionEnabled = true;
settings.textRecognition = new Scandit.TextRecognitionSettings();
settings.workingRange = Scandit.ScanSettings.WorkingRange.STANDARD;
settings.codeCachingDuration = 1;
settings.codeDuplicateFilter = 1;

const scanCase = Scandit.ScanCase.acquire(settings, { didScan: console.log });
scanCase.volumeButtonToScanEnabled(true);
scanCase.scanBeepEnabled(true);
scanCase.errorSoundEnabled(true);
scanCase.setTimeout(20, Scandit.ScanCase.State.ACTIVE, Scandit.ScanCase.State.OFF);
