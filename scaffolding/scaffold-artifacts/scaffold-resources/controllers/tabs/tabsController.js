'use strict';
/**
 * Created by samueldoyle
 */
@modName@App.lazy.controller('tabsController', ['$scope', '$log', 'tabsService',
    function ($scope, $log, tabsService) {

        $scope.tabs = tabsService.getTabs();

        $scope.getActive = function () {
            return $scope.tabs.filter(function (tab) {
                return tab.active;
            })[0];
        };
    }]);
