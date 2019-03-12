
var ScanOverlay = cordova.require("com.mirasense.scanditsdk.plugin.ScanOverlay");
var ScanSettings = cordova.require("com.mirasense.scanditsdk.plugin.ScanSettings");
var ScanSession = cordova.require("com.mirasense.scanditsdk.plugin.ScanSession");
var MatrixScanSession = cordova.require("com.mirasense.scanditsdk.plugin.MatrixScanSession");
var Barcode = cordova.require("com.mirasense.scanditsdk.plugin.Barcode");
var RecognizedText = cordova.require("com.mirasense.scanditsdk.plugin.RecognizedText");
var Constraints = cordova.require("com.mirasense.scanditsdk.plugin.Constraints");

function BarcodePicker(scanSettings) {
    if (scanSettings instanceof ScanSettings) {
        this.scanSettings = scanSettings;
    } else {
        this.scanSettings = new ScanSettings();
    }

    // Keep the overlay private.
    var overlay = new ScanOverlay();
    this.getOverlayView = function() {
        return overlay;
    }

    this.isShown = false;

    this.executingCallback = false;
    this.pausedDuringCallback = false;
    this.stoppedDuringCallback = false;

    this.continuousMode = false;
    this.portraitMargins = null;
    this.landscapeMargins = null;
    this.orientations = [];
}

BarcodePicker.Orientation = {
    PORTRAIT: "portrait",
    PORTRAIT_UPSIDE_DOWN: "portraitUpsideDown",
    LANDSCAPE_RIGHT: "landscapeLeft",
    LANDSCAPE_LEFT: "landscapeRight"
}

BarcodePicker.State = {
    PAUSED : 1,
    STOPPED : 2,
    ACTIVE : 3
};

BarcodePicker.TorchMode = {
    NONE : 0,
    ON : 1,
    OFF : 2,
    TORCH_ALTERNATING : 3
};

BarcodePicker.prototype.show = function () {
    var callbacks = {};
    if (typeof arguments[0] === 'function' || arguments.length !== 1) {
        // old method signature: BarcodePicker.show(success, manual, failure)
        callbacks.didScan = arguments[0];
        callbacks.didManualSearch = arguments.length > 1 && arguments[1] || null;
        callbacks.didCancel = arguments.length > 2 && arguments[2] || null;
        callbacks.didRecognizeText = arguments.length > 3 && arguments[3] || null;
        callbacks.didRecognizeNewCodes = arguments.length > 4 && arguments[4] || null;
        callbacks.didChangeProperty = arguments.length > 5 && arguments[5] || null;
        callbacks.didFailToValidateLicense = arguments.length > 6 && arguments[6] || null;
        callbacks.didProcessFrame = arguments.length > 7 && arguments[7] || null;
    } else {
        // new method signature: BarcodePicker.show({ didScan : function() });
        callbacks = arguments[0];
    }
    // copy to options object. Previously we were directly passing 'this' to cordova,
    // but this caused the complete picker instance to be serialized.
    var options = { "continuousMode": this.continuousMode };

    if (this.portraitConstraints != null) {
        options.portraitConstraints = this.portraitConstraints;
    }
    if (this.landscapeConstraints != null) {
        options.landscapeConstraints = this.landscapeConstraints;
    }
    if (this.orientations.length > 0) {
        options.orientations = this.orientations;
    }
    
    options.isDidScanDefined = callbacks.didScan != null;
    options.shouldPassBarcodeFrame = callbacks.didProcessFrame != null;

    var picker = this;
    cordova.exec(function (args) {
        // all the events are serialized as an array, where the first argument is the event name
        // the remaining elements are the event arguments.
        var event = args[0];
        if (event === 'didManualSearch') {
            if (callbacks.didManualSearch) {
              callbacks.didManualSearch(args[1]);
            }
            return;
        } else if (event === 'didScan') {
            picker.executingCallback = true;
            picker.pausedDuringCallback = false;
            picker.stoppedDuringCallback = false;
            var session = args[1];
            var newlyRecognized = BarcodePicker.codeArrayFromGenericArray(session.newlyRecognizedCodes);
            var newlyLocalized = BarcodePicker.codeArrayFromGenericArray(session.newlyLocalizedCodes);
            var all = BarcodePicker.codeArrayFromGenericArray(session.allRecognizedCodes);
            var properSession = new ScanSession(newlyRecognized, newlyLocalized, all, picker);
            var exceptionRaisedDuringDidScan = null;
            if (callbacks.didScan) {
                // catch exception thrown in the callback, so we can release the lock held for
                // synchronizing didScan in the picker. Otherwise we would keep the lock forever.
                try {
                    callbacks.didScan(properSession);
                } catch(e) {
                    exceptionRaisedDuringDidScan = e;
                }
            }

            // inform plugin that callback has finished executing. Required for synchronization on
            // Android/iOS. Windows doesn't require it.
            var nextStep = 0;
            if (picker.stoppedDuringCallback) {
                nextStep = 2;
            } else if (picker.pausedDuringCallback) {
                nextStep = 1;
            }
            cordova.exec(null, null, "ScanditSDK", "finishDidScanCallback",
                         [nextStep, properSession.rejectedCodes]);
            picker.executingCallback = false;
            if (exceptionRaisedDuringDidScan) {
                throw exceptionRaisedDuringDidScan;
            }
            return;
        } else if (event === 'didRecognizeText') {
            picker.executingCallback = true;
            var originalRecognizedText = args[1];
            var recognizedText = new RecognizedText(originalRecognizedText.text);
            if (callbacks.didRecognizeText) {
                var desiredState = null;
                try {
                   desiredState = callbacks.didRecognizeText(recognizedText) || BarcodePicker.State.ACTIVE;
                } catch(e) {
                   console.log('event ' + event + ' failed:' + e);
                }
            }

            // inform plugin that callback has finished executing. Required for synchronization on
            // Android/iOS. Windows doesn't require it.
            var nextStep = desiredState || BarcodePicker.State.ACTIVE;
            if (picker.stoppedDuringCallback) {
                nextStep = BarcodePicker.State.STOPPED;
            } else if (picker.pausedDuringCallback) {
                nextStep = BarcodePicker.State.PAUSED;
            }
            var rejectedCodes = [];
            if (recognizedText.rejected) {
                rejectedCodes = [1];
            }
            cordova.exec(null, null, "ScanditSDK", "finishDidScanCallback", [nextStep, rejectedCodes]);
            picker.executingCallback = false;
        } else if (event === 'didChangeState') {
            if (callbacks.didChangeState) {
                callbacks.didChangeState(args[1]);
            }
        } else if (event === 'didRecognizeNewCodes') {
            picker.executingCallback = true;
            picker.pausedDuringCallback = false;
            picker.stoppedDuringCallback = false;
            var session = args[1];
            var newlyTrackedCodes = BarcodePicker.codeArrayFromGenericArray(session.newlyTrackedCodes);

            var exceptionRaisedDuringDidRecognizeNewCodes = null;
            var matrixScanSession = new MatrixScanSession(newlyTrackedCodes);
            if (callbacks.didRecognizeNewCodes) {
                // Catch exception thrown in the callback, so we can release the lock held for
                // synchronizing didRecognizeNewCodes in the picker. Otherwise we would keep the lock forever.
                try {
                    callbacks.didRecognizeNewCodes(matrixScanSession);
                } catch(e) {
                    exceptionRaisedDuringDidRecognizeNewCodes = e;
                }
            }

            // Inform plugin that callback has finished executing. Required for synchronization on
            // Android/iOS. Windows doesn't require it.
            cordova.exec(null, null, "ScanditSDK", "finishDidRecognizeNewCodesCallback",
                         [matrixScanSession.rejectedTrackedCodes, matrixScanSession.trackedCodeStates]);
            picker.executingCallback = false;
            if (exceptionRaisedDuringDidRecognizeNewCodes) {
                throw exceptionRaisedDuringDidRecognizeNewCodes;
            }
            return;
        } else if (event === 'didChangeProperty') {
            if (callbacks.didChangeProperty) {
                callbacks.didChangeProperty(args[1].name, args[1].newState);
            }
        } else if (event === 'didFailToValidateLicense') {
            if (callbacks.didFailToValidateLicense) {
                callbacks.didFailToValidateLicense(args[1]);
            }
        } else if (event === 'didProcessFrame') {
            if (callbacks.didProcessFrame) {
                callbacks.didProcessFrame(args[1])
            }
        }
    }, callbacks.didCancel, "ScanditSDK", "show", [this.scanSettings, options, this.getOverlayView()]);

    this.isShown = true;
    this.getOverlayView().pickerIsShown = true;
}

BarcodePicker.codeArrayFromGenericArray = function(genericArray) {
    var codeArray = [];
    for (var i = 0; i < genericArray.length; i++) {
        var src = genericArray[i];
        var code = new Barcode(src, src);
        code.symbology = src.symbology;
        code.data = src.data;
        code.rawData = src.rawData;
        code.compositeFlag = src.compositeFlag;
        code.uniqueId = src.uniqueId || 0;
        codeArray.push(code);
    }
    return codeArray;
}

BarcodePicker.prototype.applyScanSettings = function(settings) {
    if (settings instanceof ScanSettings) {
        this.scanSettings = settings;
        if (this.isShown) {
            cordova.exec(null, null, "ScanditSDK", "applySettings", [settings]);
        }
    }
}

BarcodePicker.prototype.cancel = function() {
    this.isShown = false;
    this.getOverlayView().pickerIsShown = false;
    cordova.exec(null, null, "ScanditSDK", "cancel", []);
}

BarcodePicker.prototype.startScanning = function(paused) {
    if (!this.isShown) {
        return;
    }
    var options = {
        "paused": force_to_bool(paused)
    };
    cordova.exec(null, null, "ScanditSDK", "start", [options]);
}

function force_to_bool(value) { return !!value; }

BarcodePicker.prototype.stopScanning = function() {
    if (this.isShown) {
        if (this.executingCallback && (!cordova.platformId || cordova.platformId !== "windows")) {
            this.stoppedDuringCallback = true;
        } else {
            cordova.exec(null, null, "ScanditSDK", "stop", []);
        }
    }
}

BarcodePicker.prototype.pauseScanning = function() {
    if (this.isShown) {
        if (this.executingCallback && (!cordova.platformId || cordova.platformId !== "windows")) {
            this.pausedDuringCallback = true;
        } else {
            cordova.exec(null, null, "ScanditSDK", "pause", []);
        }
    }
}

BarcodePicker.prototype.resumeScanning = function() {
    if (this.isShown) {
        cordova.exec(null, null, "ScanditSDK", "resume", []);
    }
}

BarcodePicker.prototype.switchTorchOn = function(enabled) {
    if (this.isShown) {
        cordova.exec(null, null, "ScanditSDK", "torch", [enabled]);
    }
}

BarcodePicker.prototype.setOrientations = function(orientations) {
    this.orientations = orientations;
    if (this.isShown) {
        cordova.exec(null, null, "ScanditSDK", "updateOverlay", [{"orientations": orientations}]);
    }
}

BarcodePicker.prototype.setConstraints = function(portrait, landscape, animationDuration) {
    if (portrait == null) {
        this.portraitConstraints = new Constraints();
    } else {
        this.portraitConstraints = portrait;
    }
    if (landscape == null) {
        this.landscapeConstraints = new Constraints();
    } else {
        this.landscapeConstraints = landscape;
    }
    if (this.isShown) {
        var duration = 0;
        if (typeof animationDuration !== "undefined") {
            duration = parseFloat(animationDuration);
        }
        cordova.exec(null, null, "ScanditSDK", "resize", [{"portraitConstraints": this.portraitConstraints,
                                                           "landscapeConstraints": this.landscapeConstraints,
                                                           "animationDuration": duration}]);
    }
}

BarcodePicker.prototype.setMargins = function(portrait, landscape, animationDuration) {
    var portraitConstraints = null;
    var landscapeConstraints = null;

    if (portrait != null) {
        portraitConstraints = new Constraints();
        portraitConstraints.leftMargin = portrait.left;
        portraitConstraints.topMargin = portrait.top;
        portraitConstraints.rightMargin = portrait.right;
        portraitConstraints.bottomMargin = portrait.bottom;
    }
    if (landscape != null) {
        landscapeConstraints = new Constraints();
        landscapeConstraints.leftMargin = landscape.left;
        landscapeConstraints.topMargin = landscape.top;
        landscapeConstraints.rightMargin = landscape.right;
        landscapeConstraints.bottomMargin = landscape.bottom;
    }
    this.setConstraints(portraitConstraints, landscapeConstraints, animationDuration);
}

module.exports = BarcodePicker;
