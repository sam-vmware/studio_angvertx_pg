'use strict';
// vamiApp.lazy.controller('serviceTabsController', ['$q', '$scope', '$log', 'serviceTabsService', function ($q, $scope, $log, serviceTabsService) {
vamiApp.controller('systemTabController', ['$q', '$scope', '$routeParams', '$log', '$state', 'serviceTabsService',
    function ($q, $scope, $routeParams, $log, $state, serviceTabsService) {

        var testMsg = {
            type: "SystemInformation",
            operation: "testGetSystemInformation"
        };

        /* reply.data = {
         key hostName: value: "sam-MacBookPro"
         keY: osName: value: "Ubuntu"
         keY: product: value: "VMware Studio"
         keY: vendor: value: "VMware, Inc."
         keY: version: value: "2.7.0.0 Build 140209153907"
         }*/

        $scope.contents = [];

        $scope.getContent = function () {
            $scope.contents.length = 0;
            var options = {
                serviceName: "system",
                jsonMsg: testMsg
            };
            $.when(serviceTabsService.getTabContent(options)).then(function (reply) {
                var newContent = [];
                angular.forEach(reply.data, function (item, key) {
                    newContent[(item.key)] = item.value;
                });

                $scope.$apply(function () {
                    $scope.contents.length = 0;
                    $scope.contents = newContent;
                });
            });
        };

        $scope.test = function() {
            $log.debug("HERE I AM");
        };

        //$scope.getContent();
    }]);
