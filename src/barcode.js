
function Barcode(gs1DataCarrier, recognized) {
    this.isGs1DataCarrier = function() {
        return gs1DataCarrier;
    }
    this.isRecognized = function() {
        return recognized;
    }
}

Barcode.Symbology = {
    UNKNOWN: "unknown",
    EAN13: "ean13",
    EAN8: "ean8",
    UPC12: "upca",
    UPCA: "upca",
    UPCE: "upce",
    CODE11: "code11",
    CODE128: "code128",
    CODE32: "code32",
    CODE39: "code39",
    CODE93: "code93",
    CODE25: "code25",
    ITF: "itf",
    QR: "qr",
    DATA_MATRIX: "data-matrix",
    PDF417: "pdf417",
    MICRO_PDF417: "micropdf417",
    MSI_PLESSEY: "msi-plessey",
    GS1_DATABAR: "databar",
    GS1_DATABAR_LIMITED: "databar-limited",
    GS1_DATABAR_EXPANDED: "databar-expanded",
    CODABAR: "codabar",
    AZTEC: "aztec",
    DOTCODE: "dotcode",
    MAXICODE: "maxicode",
    FIVE_DIGIT_ADD_ON: "five-digit-add-on",
    TWO_DIGIT_ADD_ON: "two-digit-add-on",
    KIX: "kix",
    RM4SCC: "rm4scc",
    MICRO_QR: "microqr",
    LAPA4SC: "lapa4sc"
}

module.exports = Barcode;
