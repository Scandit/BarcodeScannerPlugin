(function (exports) {

    exports.loadTabHome = function ($scope) {
	if ($scope.ready) {
	    $scope.stopPicker();
	} else {
	    document.addEventListener('deviceready', $scope.stopPicker);
	}

        $scope.setMargin("0%", "0%", "0%", "50%");
        $scope.setLandscapeMargin("0%", "0%", "0%", "50%");
        var scanResults = document.getElementById('scanResults');
        $scope.setCallback(function (session, manual) {
           $scope.pausePicker();
           if (manual) {
               var code = session;
               $scope.$apply(function () {
                   $scope.scannedCode = 'Manual entry: ' + code;
               });
           } else {
               var code = session.newlyRecognizedCodes[0];
               $scope.$apply(function () {
                   $scope.scannedCode = '(' + code.symbology.toUpperCase() + ') ' + code.data;
                });
            }
        });
        if ($scope.ready) {
           $scope.startPicker();
       } else {
            document.addEventListener('deviceready', $scope.startPicker);
       }
    }
})(this);
