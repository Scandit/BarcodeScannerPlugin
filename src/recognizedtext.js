cordova.define("com.mirasense.scanditsdk.plugin.RecognizedText", function(require, exports, module) {

function RecognizedText(text) {
	this.text = text;
	this.rejected = false;
}

module.exports = RecognizedText;

});
