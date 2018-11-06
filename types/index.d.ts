declare module Scandit {
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
        constructor(settings: ScanSettings);
        continuousMode: boolean;
        orientations: BarcodePicker.Orientation[];

        show(
            didScan: Function,
            didManualSearch?: Function,
            didCancel?: Function,
            didRecognizeText?: Function,
            didRecognizeNewCodes?: Function,
            didChangeProperty?: Function,
        ): void;
        show(callbacks: Callbacks): void;

        cancel(): void;
        applyScanSettings(scanSettings: ScanSettings): void;
        setOrientations(orientations: BarcodePicker.Orientation[]): void;
        setConstraints(portraitConstraints: Constraints, landscapeConstraints: Constraints, animationDuration: number): void;
        setMargins(portraitMargins: Margins, landscapeMargins: Margins, animationDuration: number): void;
        pauseScanning(): void;
        resumeScanning(): void;
        stopScanning(): void;
        startScanning(): void;
        startScanning(startInPausedState: boolean): void;
        switchTorchOn(on: boolean): void;
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
        didScan?(scanSession: ScanSession): void;
        didManualSearch?(text: string): void;
        didCancel?(reason: any): void;
        didRecognizeText?(recognizedText: RecognizedText): void;
        didRecognizeNewCodes?(matrixScanSession: MatrixScanSession): void;
        didChangeState?(newState: BarcodePicker.State): void;
        didChangeProperty?(propertyName: string, newValue: any): void;
        didFailToValidateLicense?(error: LicenseError): void;
        didProcessFrame?(base64Frame: Frame): void;
    }

    export class Frame {
        base64Data: string;
    }

    export class LicenseError {
        message: string
    }

    export class Constraints {
        leftMargin?: number | string;
        topMargin?: number | string;
        rightMargin?: number | string;
        bottomMargin?: number | string;
        width?: number | string;
        height?: number | string;
    }

    export namespace License {
        function setAppKey(appKey: string): void;
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

        rejectTrackedCode(barcode: Barcode): void;
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

        volumeButtonToScanEnabled(enabled: boolean): void;
        scanBeepEnabled(enabled: boolean): void;
        errorSoundEnabled(enabled: boolean): void;
        setTimeout(timeout: number, fromState: ScanCase.State, toState: ScanCase.State): void;
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
        setGuiStyle(guiStyle: ScanOverlay.GuiStyle): void;

        setBeepEnabled(enabled: boolean): void;
        setVibrateEnabled(enabled: boolean): void;

        setTorchEnabled(enabled: boolean): void;
        setTorchButtonMarginsAndSize(leftMargin: number, topMargin: number, width: number, height: number): void;
        setTorchButtonOffAccessibility(label: string, hint: string): void;
        setTorchButtonOnAccessibility(label: string, hint: string): void;

        setCameraSwitchVisibility(visibility: ScanOverlay.CameraSwitchVisibility): void;
        setCameraSwitchButtonMarginsAndSize(rightMargin: number, topMargin: number, width: number, height: number): void;
        setCameraSwitchButtonBackAccessibility(label: string, hint: string): void;
        setCameraSwitchButtonFrontAccessibility(label: string, hint: string): void;

        setViewfinderDimension(width: number, height: number, landscapeWidth: number, landscapeHeight: number): void;
        setViewfinderColor(hexCode: string): void;
        setViewfinderDecodedColor(hexCode: string): void;

        showSearchBar(show: boolean): void;
        setSearchBarActionButtonCaption(caption: string): void;
        setSearchBarCancelButtonCaption(caption: string): void;
        setSearchBarPlaceholderText(text: string): void;
        setMinSearchBarBarcodeLength(length: number): void;
        setMaxSearchBarBarcodeLength(length: number): void;
        setToolBarButtonCaption(caption: string): void;
        setProperty(key: string, value: any): void;
        setTextRecognitionSwitchVisible(visible: boolean): void;
        setMissingCameraPermissionInfoText(missingCameraPermissionInfoText: string): void;
        updateOverlayIfExists(): void;
        setMatrixScanHighlightingColor(state: ScanOverlay.MatrixScanState, hexCode: string): void;
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

        pauseScanning(): void;
        stopScanning(): void;
        rejectCode(barcode: Barcode): void;
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
        setSymbologyEnabled(symbology: Barcode.Symbology, enabled: boolean): void;
        setProperty(key: string, value: any): void;
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
