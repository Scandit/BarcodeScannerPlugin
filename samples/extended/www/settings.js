(function (exports) {
    var app = angular.module('app');
    var SYMBOLOGIES = [
           { ids: ["ean13", "upca"], title: "EAN-13 & UPC-A", enabled: true },
           { ids: ["ean8"], title: "EAN-8", enabled: true },
           { ids: ["upce"], title: "UPC-E", enabled: true },
           { ids: ["code39"], title: "Code 39", enabled: true },
           { ids: ["code93"], title: "Code 93", enabled: false },
           { ids: ["code128"], title: "Code 128", enabled: true },
           { ids: ["itf"], title: "Interleaved 2 of 5", enabled: true },
           { ids: ["msi-plessey"], title: "MSI Plessey", enabled: false },
           { ids: ["code11"], title: "Code 11", enabled: false },
           { ids: ["codabar"], title: "Codabar", enabled: false },
           { ids: ["qr"], title: "QR", enabled: true },
           { ids: ["data-matrix"], title: "Data Matrix", enabled: true },
           { ids: ["aztec"], title: "Aztec", enabled: false },
           { ids: ["maxicode"], title: "Maxi Code", enabled: false },
    ];
    var ui = {
        beep: true,
        vibrate: true,
        style: 'default',
        searchBar: false,
        torch: true,
        torch_xmargin: 15,
        torch_ymargin: 15,
        cameraSwitch: 'never',
        cameraSwitch_xmargin: 15,
        cameraSwitch_ymargin: 15,
        viewfinder_width: 0.8,
        viewfinder_height: 0.4,
        viewfinderLandscape_width: 0.6,
        viewfinderLandscape_height: 0.4
    };

    if ((localStorage.ui !== null) && (localStorage.ui !== undefined)) {
        var list = JSON.parse(localStorage.ui);
        for (i in ui) {
            if (list[i] !== null && list[i] !== undefined) {
	        if (typeof ui[i] === typeof list[i]) {  
                    ui[i] = list[i];
		}
            }
        }
    }
    if ((localStorage.symbologies !== null) && (localStorage.symbologies !== undefined)) {
        list = JSON.parse(localStorage.symbologies);
        for (sym in SYMBOLOGIES) {
            for (sym2 in list) {
                if (SYMBOLOGIES[sym]["title"] === list[sym2]["title"]
		    && typeof list[sym2]["enabled"] === "boolean") {
                        SYMBOLOGIES[sym] = list[sym2];
                }
            }
        }
    }

    app.controller("SettingsController", function ($scope) {
        $scope.symbologies = SYMBOLOGIES
        $scope.ui = ui;
        $scope.saveSettings = function () {
            localStorage.setItem("ui", JSON.stringify($scope.ui));
            localStorage.setItem("symbologies", JSON.stringify($scope.symbologies));
        }
    });

    exports.getScanSettings = function () {
        var scanSettings = new Scandit.ScanSettings();
        SYMBOLOGIES.forEach(function (s) {
            s.ids.forEach(function (id) {
                scanSettings.setSymbologyEnabled(id, s.enabled);
            });
        });
        return scanSettings;
    };

    exports.applyUISettings = function (picker) {
        var overlay = picker.getOverlayView();
        overlay.setBeepEnabled(ui.beep);
        overlay.setVibrateEnabled(ui.vibrate);
        overlay.showSearchBar(ui.searchBar);
        if (ui.searchBar) {
            overlay.setSearchBarPlaceholderText("Manual barcode data entry");
        }
        overlay.setTorchEnabled(ui.torch);
        overlay.setTorchButtonMarginsAndSize(parseInt(ui.torch_xmargin), parseInt(ui.torch_ymargin), 40, 40);
        overlay.setCameraSwitchVisibility(ui.torch);
        overlay.setCameraSwitchButtonMarginsAndSize(parseInt(ui.cameraSwitch_xmargin), parseInt(ui.cameraSwitch_ymargin), 40, 40);
        overlay.setViewfinderDimension(parseFloat(ui.viewfinder_width),
                        parseFloat(ui.viewfinder_height),
                        parseFloat(ui.viewfinderLandscape_width),
                        parseFloat(ui.viewfinderLandscape_height));
        if (ui.cameraSwitch === 'always') {
            overlay.setCameraSwitchVisibility(Scandit.ScanOverlay.CameraSwitchVisibility.ALWAYS);
        } else if (ui.cameraSwitch === 'tablet') {
            overlay.setCameraSwitchVisibility(Scandit.ScanOverlay.CameraSwitchVisibility.ON_TABLET);
        } else if (ui.cameraSwitch === 'never') {
            overlay.setCameraSwitchVisibility(Scandit.ScanOverlay.CameraSwitchVisibility.NEVER);
        }
        if (ui.style === 'default') {
            overlay.setGuiStyle(Scandit.ScanOverlay.GuiStyle.DEFAULT);
        } else if (ui.style === 'laser') {
            overlay.setGuiStyle(Scandit.ScanOverlay.GuiStyle.LASER);
        } else {
            overlay.setGuiStyle(Scandit.ScanOverlay.GuiStyle.NONE);
        }
    }

    exports.loadTabSettings = function ($scope) {
        if ($scope.ready) {
            $scope.stopPicker();
        } else {
            document.addEventListener('deviceready', $scope.stopPicker);
        }
    }

})(this);
