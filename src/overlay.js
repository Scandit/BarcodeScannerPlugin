
function ScanOverlay() {
    this.pickerIsShown = false;
    this.properties = {};
}

ScanOverlay.CameraSwitchVisibility = {
    NEVER: 0,
    ON_TABLET: 1,
    ALWAYS: 2
}

ScanOverlay.GuiStyle = {
	DEFAULT: 0,
	LASER: 1,
	NONE: 2,
	MATRIXSCAN: 3,
    MATRIX_SCAN: 3,
	LOCATIONSONLY: 4,
    LOCATIONS_ONLY: 4
}

ScanOverlay.prototype.setGuiStyle = function(guiStyle) {
    this.guiStyle = guiStyle;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setBeepEnabled = function(enabled) {
    this.beep = enabled;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setVibrateEnabled = function(enabled) {
    this.vibrate = enabled;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setTorchEnabled = function(enabled) {
    this.torch = enabled;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setTorchButtonMarginsAndSize = function(leftMargin, topMargin, width, height) {
    this.torchButtonMarginsAndSize = [leftMargin, topMargin, width, height];
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setTorchButtonOffAccessibility = function(label, hint) {
    this.torchButtonOffAccessibilityLabel = label;
    this.torchButtonOffAccessibilityHint = hint;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setTorchButtonOnAccessibility = function(label, hint) {
    this.torchButtonOnAccessibilityLabel = label;
    this.torchButtonOnAccessibilityHint = hint;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setCameraSwitchVisibility = function(visibility) {
    this.cameraSwitchVisibility = visibility;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setCameraSwitchButtonMarginsAndSize = function(rightMargin, topMargin, width, height) {
    this.cameraSwitchButtonMarginsAndSize = [rightMargin, topMargin, width, height];
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setCameraSwitchButtonBackAccessibility = function(label, hint) {
    this.cameraSwitchButtonBackAccessibilityLabel = label;
    this.cameraSwitchButtonBackAccessibilityHint = hint;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setCameraSwitchButtonFrontAccessibility = function(label, hint) {
    this.cameraSwitchButtonFrontAccessibilityLabel = label;
    this.cameraSwitchButtonFrontAccessibilityHint = hint;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setViewfinderDimension = function(width, height, landscapeWidth, landscapeHeight) {
    this.viewfinderDimension = [width, height, landscapeWidth, landscapeHeight];
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setViewfinderColor = function(hexCode) {
    this.viewfinderColor = hexCode;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setViewfinderDecodedColor = function(hexCode) {
    this.viewfinderDecodedColor = hexCode;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.showSearchBar = function(show) {
    this.searchBar = show;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setSearchBarActionButtonCaption = function(caption) {
    this.searchBarActionButtonCaption = caption;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setSearchBarCancelButtonCaption = function(caption) {
    this.searchBarCancelButtonCaption = caption;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setSearchBarPlaceholderText = function(text) {
    this.searchBarPlaceholderText = text;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setMinSearchBarBarcodeLength = function(length) {
    this.minSearchBarBarcodeLength = length;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setMaxSearchBarBarcodeLength = function(length) {
    this.maxSearchBarBarcodeLength = length;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setToolBarButtonCaption = function(caption) {
    this.toolBarButtonCaption = caption;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setProperty = function(key, value) {
    this.properties[key] = value;
    this.updateOverlayIfExists();
}

ScanOverlay.prototype.setTextRecognitionSwitchVisible = function(visible) {
    this.textRecognitionSwitchVisible = visible;
	this.updateOverlayIfExists();
}

ScanOverlay.prototype.setMissingCameraPermissionInfoText = function(missingCameraPermissionInfoText) {
	this.missingCameraPermissionInfoText = missingCameraPermissionInfoText;
	this.updateOverlayIfExists();
}

ScanOverlay.prototype.updateOverlayIfExists = function() {
    if (this.pickerIsShown) {
        cordova.exec(null, null, "ScanditSDK", "updateOverlay", [this]);
    }
}


module.exports = ScanOverlay;
