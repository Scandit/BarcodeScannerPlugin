angular.module('app', ['onsen']);

angular.module('app').controller('AppController', function ($scope) {
   var picker = null;
   var margins = [0, 0, 0, 0];
   var landscapeMargins = [0, 0, 0, 0];
   var callback = null;
   $scope.ready = false;
   document.addEventListener('deviceready', function () { $scope.ready = true; });
   $scope.scannedCode = '';
   $scope.paused = false;

   $scope.startPicker = function () {
       $scope.paused = false;
       picker = initPicker(getScanSettings(), margins, landscapeMargins);
       applyUISettings(picker);
       picker.show(function (session) {
           callback(session, false);
       }, function (session) {
           callback(session, true); 
       });
       picker.startScanning();
   };

   $scope.pausePicker = function () {
       picker.pauseScanning();
       $scope.paused = true;
   };

   $scope.resumePicker = function () {
       picker.resumeScanning();
       $scope.paused = false;
   };

   $scope.stopPicker = function () {
       if (picker !== null) {
           picker.cancel();
           picker = null;
       }
       $scope.paused = true;
   };

   $scope.setMargin = function (marginLeft, marginTop, marginRight, marginBottom) {
       margins = [marginLeft, marginTop, marginRight, marginBottom];
   };

   $scope.setLandscapeMargin = function (marginLeft, marginTop, marginRight, marginBottom) {
       landscapeMargins = [marginLeft, marginTop, marginRight, marginBottom];
   };

   $scope.setCallback = function (cb) {
        callback = cb;
   };

   $scope.continueScanning = function () {
       var scanResults = document.getElementById('scanResults');
       scanResults.style.display = 'none';
   };
   
   $scope.onTabChange = function () {
       if (mainTabbar.getActiveTabIndex() === 0) {
           loadTabHome($scope);
       } else if (mainTabbar.getActiveTabIndex() === 1) {
           loadTabSettings($scope);
       } else if (mainTabbar.getActiveTabIndex() === 2) {
           loadTabPickers($scope);
       }
   };

});


function initPicker(scanSettings, margins, lMargins) {
    Scandit.License.setAppKey("--- ENTER YOUR SCANDIT APP KEY HERE ---");
    
    // Instantiate the barcode picker by using the settings defined above.
    var picker = new Scandit.BarcodePicker(scanSettings);
    if ((parseInt(margins[0])>0) || (parseInt(margins[1])>0)
        || (parseInt(margins[2])>0) || (parseInt(margins[3])>0)
        || (parseInt(lMargins[0])>0) || (parseInt(lMargins[1])>0)
        || (parseInt(lMargins[2])>0) || (parseInt(lMargins[3])>0)) {
        picker.setMargins(new Scandit.Margins(margins[0], margins[1], margins[2], margins[3]),
                          new Scandit.Margins(lMargins[0], lMargins[1], lMargins[2], lMargins[3]));
    }
    // when continuous mode is false, scanning stops after the first code
    // has been successfully scanned. Set this property to true to continue
    // scanning. In that case it is up to you to stop/pause the scanning
    // process using PICKER.pauseScanning(), or PICKER.resumeScanning().
    picker.continuousMode = true;
    return picker;
}



(function () {
 "use strict";
 
 document.addEventListener( 'deviceready', onDeviceReady.bind( this ), false );
 
 function onDeviceReady() {
 // Handle the Cordova pause and resume events
 document.addEventListener( 'pause', onPause.bind( this ), false );
 document.addEventListener( 'resume', onResume.bind( this ), false );
 
 };
 
 function onPause() {
 };
 
 function onResume() {
 };
 } )();
