var Rect = cordova.require("com.mirasense.scanditsdk.plugin.Rect");


function TextRecognitionSettings() {
    this.symbologies = {};

    this.areaPortrait = new Rect(0, 0.375, 1, 0.25);
    this.areaLandscape = new Rect(0, 0.375, 1, 0.25);
}

module.exports = TextRecognitionSettings;
