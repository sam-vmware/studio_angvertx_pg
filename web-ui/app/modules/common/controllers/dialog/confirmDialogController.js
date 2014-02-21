vamiApp.lazy.controller('confirmDialogController', ['$scope', '$log', '$modalInstance', 'modalData',
    function ($scope, $log, $modalInstance, modalData) {
        $scope.modalData = modalData;

        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

        $log.debug("modalData", modalData);
    }]);


