'use strict';
vamiApp.lazy.controller('serviceTabsController',  ['$scope','serviceTabsService', function($scope, serviceTabsService) {
    $scope.tabs = serviceTabsService.getAllTabs();

    $scope.navType = 'pills';
}]);
