'use strict';
// vamiApp.lazy.controller('serviceTabsController', ['$q', '$scope', '$log', 'serviceTabsService', function ($q, $scope, $log, serviceTabsService) {
vamiApp.controller('systemTabController', ['$q', '$scope', '$routeParams', '$log', '$state', '$timeout', 'serviceTabsService',
    function ($q, $scope, $routeParams, $log, $state, $timeout, serviceTabsService) {

        var testGetSystemInformation = {
            type: "SystemInformation",
            operation: "testGetSystemInformation"
        };

        var testDoReboot = {
            type     : "OperatingSystem",
            operation: "testDoReboot"
        };

        var testDoShutdown = {
            type     : "OperatingSystem",
            operation: "testDoShutdown"
        };

        /* reply.data = {
         key hostName: value: "sam-MacBookPro"
         keY: osName: value: "Ubuntu"
         keY: product: value: "VMware Studio"
         keY: vendor: value: "VMware, Inc."
         keY: version: value: "2.7.0.0 Build 140209153907"
         }*/

        $scope.contents = [];

        $scope.columnDefs = [
            {field: "key", displayName: "key"},
            {field: "value", displayName: "value"}
        ];
        $scope.gridOptions = {
            data: "contents",
            columnDefs: $scope.columnDefs
        };

        $scope.doReboot = function() {
            $log.debug("Reboot operation selected");
        };

        $scope.doShutdown = function() {
            $log.debug("Shutdown operation selected");
        };

        $scope.clickToggle = function ($event) {
            var elem = angular.element($event.srcElement);
            if (elem.hasClass("active")) {
                elem.removeClass("active");
            } else {
                elem.addClass("active");
            }
        };

        $scope.$watch("contents", function () {
            $scope.columnDefs = [];
            angular.forEach(_.keys($scope.contents), function (key) {
                $scope.columnDefs.push({field: key});
            });
        });

        $scope.getContent = function () {
            var options = {
                serviceName: "system",
                jsonMsg: testGetSystemInformation
            };

            // We get back a promise
            serviceTabsService.sendRequest(options).then(function (reply) {
                /*                var newContent = [];
                 angular.forEach(reply.data, function (item, key) {
                 newContent[(item.key)] = item.value;
                 });*/

                var timer = $timeout(function () {
                    $scope.$apply(function () {
                        if ($scope.contents) $scope.contents.length = 0;
                        $scope.contents = reply.data;
                    });
                }, 1000);

                timer.then(
                    function () {
                        $log.debug("Timer resolved!", Date.now());
                    },
                    function () {
                        $log.warn("Timer rejected!", Date.now());
                    }
                );

                // Cleanup timer
                $scope.$on(
                    "$destroy",
                    function (event) {
                        if (timer) {
                            $timeout.cancel(timer);
                        }
                    }
                );

            });
        };

        /*        $scope.test = function() {
         $log.debug("HERE I AM");
         };*/

        $scope.getContent();
    }]);
