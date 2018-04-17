declare module 'barcodescanner-sdk-cordova' {
    export module Scandit {
        export class Barcode {
            isGs1DataCarrier(): boolean;
            isRecognized(): boolean;
    
            symbology: Barcode.Symbology;
            data: string;
            rawData: number[];
            compositeFlag: Barcode.CompositeFlag;
            location: Quadrilateral;
        }
        export module Barcode {
            export enum Symbology {
                UNKNOWN,
                EAN13,
                EAN8,
                UPC12,
                UPCA,
                UPCE,
                CODE11,
                CODE128,
                CODE39,
                CODE93,
                CODE25,
                ITF,
                QR,
                DATA_MATRIX,
                PDF417,
                MICRO_PDF417,
                MSI_PLESSEY,
                GS1_DATABAR,
                GS1_DATABAR_LIMITED,
                GS1_DATABAR_EXPANDED,
                CODABAR,
                AZTEC,
                DOTCODE,
                MAXICODE,
                FIVE_DIGIT_ADD_ON,
                TWO_DIGIT_ADD_ON,
                KIX,
                RM4SCC,
                MICRO_QR,
            }
    
            export enum CompositeFlag {
                NONE,
                UNKNOWN,
                LINKED,
                GS1_TYPE_A,
                GS1_TYPE_B,
                GS1_TYPE_C,
            }
        }
    
        export class BarcodePicker {
            continuousMode: boolean;
            orientations: BarcodePicker.Orientation[];
    
            show(didScan: Function,
                didManualSearch?: Function,
                didCancel?: Function,
                didRecognizeText?: Function,
                didRecognizeNewCodes?: Function,
                didChangeProperty?: Function, )
            show(callbacks: Callbacks);
    
            cancel();
            applyScanSettings(scanSettings: ScanSettings);
            setOrientations(orientations: BarcodePicker.Orientation[]);
            setConstraints(portraitConstraints: Constraints, landscapeConstraints: Constraints, animationDuration: number);
            setMargins(portraitMargins: Margins, landscapeMargins: Margins, animationDuration: number);
            pauseScanning();
            resumeScanning();
            stopScanning();
            startScanning();
            startScanning(startInPausedState: boolean);
            switchTorchOn(on: boolean);
            getOverlayView(): ScanOverlay;
            convertPointToPickerCoordinates(point: Point): Point;
        }
        export module BarcodePicker {
            export enum Orientation {
                PORTRAIT,
                PORTRAIT_UPSIDE_DOWN,
                LANDSCAPE_RIGHT,
                LANDSCAPE_LEFT,
            }
    
            export enum State {
                STOPPED,
                ACTIVE,
                PAUSED,
            }
    
            export enum TorchMode {
                NONE,
                ON,
                OFF,
                TORCH_ALTERNATING
            }
        }
    
        export class Callbacks {
            didScan?(scanSession: ScanSession);
            didManualSearch?(text: String);
            didCancel?(reason: any);
            didRecognizeText?(recognizedText: RecognizedText);
            didRecognizeNewCodes?(matrixScanSession: MatrixScanSession);
            didChangeState?(newState: BarcodePicker.State);
            didChangeProperty?(propertyName: string, newValue: any);
            didFailToValidateLicense?(error: LicenseError);
        }

        export class LicenseError {
            message: string
        }
    
        export class Constraints {
            leftMargin?: number;
            topMargin?: number;
            rightMargin?: number;
            bottomMargin?: number;
            width?: number;
            height?: number;
        }
    
        export namespace License {
            function setAppKey(appKey: string);
        }
    
        export class Margins {
            constructor(left: number, top: number, right: number, bottom: number);
            constructor(left: String, top: String, right: String, bottom: String);
            left: number | String;
            top: number | String;
            right: number | String;
            bottom: number | String;
        }
    
        export class MatrixScanSession {
            constructor(newlyTrackedCodes: Barcode[]);
            newlyTrackedCodes: Barcode[];
    
            rejectTrackedCode(barcode: Barcode);
        }
    
        export class Point {
            constructor(x: number, y: number);
            x: number;
            y: number;
        }
    
        export class Quadrilateral {
            constructor(topLeft: Point, topRight: Point, bottomLeft: Point, bottomRight: Point);
            topLeft: Point;
            topRight: Point;
            bottomLeft: Point;
            bottomRight: Point;
        }
    
        export class RecognizedText {
            constructor(text: string)
            text: string;
            rejected: boolean;
        }
    
        export class Rect {
            constructor(x: number, y: number, width: number, height: number);
            x: number;
            y: number;
            width: number;
            height: number;
        }
    
        export class ScanCase {
            static acquire(scanSettings: ScanSettings, callbacks: ScanCase.CaseCallbacks): ScanCase;
    
            volumeButtonToScanEnabled(enabled: boolean);
            scanBeepEnabled(enabled: boolean);
            errorSoundEnabled(enabled: boolean);
            setTimeout(timeout: number, fromState: ScanCase.State, toState: ScanCase.State);
        }
        export module ScanCase {
            export class CaseCallbacks {
                didInitialize?: Function;
                didScan?: Function;
                didChangeState?: Function;
            }
    
            export enum State {
                ACTIVE,
                OFF,
                STANDBY,
            }
    
            export enum StateChangeReason {
                MANUAL,
                TIMEOUT,
                VOLUME_BUTTON,
            }
        }
    
        export class ScanOverlay {
            setGuiStyle(guiStyle: ScanOverlay.GuiStyle);
    
            setBeepEnabled(enabled: boolean);
            setVibrateEnabled(enabled: boolean);
    
            setTorchEnabled(enabled: boolean);
            setTorchButtonMarginsAndSize(leftMargin: number, topMargin: number, width: number, height: number);
            setTorchButtonOffAccessibility(label: string, hint: string);
            setTorchButtonOnAccessibility(label: string, hint: string);
    
            setCameraSwitchVisibility(visibility: ScanOverlay.CameraSwitchVisibility);
            setCameraSwitchButtonMarginsAndSize(rightMargin: number, topMargin: number, width: number, height: number);
            setCameraSwitchButtonBackAccessibility(label: string, hint: string);
            setCameraSwitchButtonFrontAccessibility(label: string, hint: string);
    
            setViewfinderDimension(width: number, height: number, landscapeWidth: number, landscapeHeight: number);
            setViewfinderColor(hexCode: string);
            setViewfinderDecodedColor(hexCode: string);
    
            showSearchBar(show: boolean);
            setSearchBarActionButtonCaption(caption: string);
            setSearchBarCancelButtonCaption(caption: string);
            setSearchBarPlaceholderText(text: string);
            setMinSearchBarBarcodeLength(length: number);
            setMaxSearchBarBarcodeLength(length: number);
            setToolBarButtonCaption(caption: string);
            setProperty(key: string, value: any);
            setTextRecognitionSwitchVisible(visible: boolean);
            setMissingCameraPermissionInfoText(missingCameraPermissionInfoText: string);
            updateOverlayIfExists();
            setMatrixScanHighlightingColor(state: ScanOverlay.MatrixScanState, hexCode: string);
        }
        export module ScanOverlay {
            export enum CameraSwitchVisibility {
                NEVER,
                ON_TABLET,
                ALWAYS,
            }
    
            export enum GuiStyle {
                DEFAULT,
                LASER,
                NONE,
                MATRIXSCAN,
                MATRIX_SCAN,
                LOCATIONSONLY,
                LOCATIONS_ONLY,
            }
    
            export enum MatrixScanState {
                LOCALIZED,
                RECOGNIZED,
                REJECTED,
            }
        }
    
        export class ScanSession {
            constructor(
                newlyRecognizedCodes: Barcode[],
                newlyLocalizedCodes: Barcode[],
                allRecognizedCodes: Barcode[],
                picker: BarcodePicker
            )
    
            newlyRecognizedCodes: Barcode[];
            newlyLocalizedCodes: Barcode[];
            allRecognizedCodes: Barcode[];
    
            pauseScanning();
            stopScanning();
            rejectCode(barcode: Barcode);
        }
    
        export class TextRecognitionSettings {
            areaPortrait?: Rect;
            areaLandscape?: Rect;
            regex?: String;
            characterWhitelist?: String;
        }
    
        export class ScanSettings {
            recognitionMode: ScanSettings.RecognitionMode;
            highDensityModeEnabled: boolean;
            activeScanningAreaPortrait: Rect;
            activeScanningAreaLandscape: Rect;
            deviceName: string;
            matrixScanEnabled: boolean;
            symbologies: Object;
            cameraFacingPreference: ScanSettings.CameraFacing;
            scanningHotSpot: Point;
            relativeZoom: number;
            maxNumberOfCodesPerFrame: number;
            codeRejectionEnabled: boolean;
            textRecognition: TextRecognitionSettings;
    
            workingRange: number;
    
            codeCachingDuration: number;
            codeDuplicateFilter: number;
    
            getSymbologySettings(symbology: Barcode.Symbology): SymbologySettings;
            setSymbologyEnabled(symbology: Barcode.Symbology, enabled: boolean);
            setProperty(key: string, value: any);
        }
    
        export module ScanSettings {
            export enum RecognitionMode {
                TEXT,
                CODE,
            }
            
            export enum CameraFacing {
                BACK,
                FRONT,
            }
            
            export enum WorkingRange {
                STANDARD,
                LONG,
            }
        }
    
        export class SymbologySettings {
            enabled?: boolean;
            colorInvertedEnabled?: boolean;
            checksums?: SymbologySettings.Checksum[];
            extensions?: SymbologySettings.Extension[];
            activeSymbolCounts?: number[];
        }
    
        export module SymbologySettings {
            export enum Checksum {
                MOD_10,
                MOD_11,
                MOD_47,
                MOD_43,
                MOD_103,
                MOD_1010,
                MOD_1110,
            }
    
            export enum Extension {
                TINY,
                FULL_ASCII,
                REMOVE_LEADING_ZERO,
            }
        }
    }
}

// import with `/// <reference path="path-to-plugin/types/index.d.ts"/>` at the top of the file where the import should happen
// or
// in tsconfig.json, "include" should include the path "path-to-plugin/types/index.d.ts"
// so one can do `import { Scandit } from "barcodescanner-sdk-cordova"`
