'use strict';
/**
 * Created by samueldoyle
 */
systemApp.controller('systemTabsController', ['$scope', '$log', 'WEB_ROOT', 'systemTabsService',
    function ($scope, $log, WEB_ROOT, systemTabsService) {

    $scope.tabs = systemTabsService.getTabs();

    $scope.getActive = function () {
        return $scope.tabs.filter(function (tab) {
            return tab.active;
        })[0];
    };

}]);
