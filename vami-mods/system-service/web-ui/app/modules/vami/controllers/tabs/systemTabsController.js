'use strict';
/**
 * Created by samueldoyle
 */
systemApp.lazy.controller('systemTabsController', ['$scope', '$log', 'systemTabsService',
    function ($scope, $log, systemTabsService) {

        $scope.tabs = systemTabsService.getTabs();

        $scope.getActive = function () {
            return $scope.tabs.filter(function (tab) {
                return tab.active;
            })[0];
        };

    }]);
