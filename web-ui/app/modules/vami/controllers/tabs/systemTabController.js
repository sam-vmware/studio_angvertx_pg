'use strict';
vamiApp.lazy.controller('systemTabController', ['$q', '$scope', '$routeParams', '$log', '$state', '$timeout', 'serviceTabsService',
// vamiApp.controller('systemTabController', ['$q', '$scope', '$routeParams', '$log', '$state', '$timeout', 'serviceTabsService',
    function ($q, $scope, $routeParams, $log, $state, $timeout, serviceTabsService) {

        var testGetSystemInformation = {
            type: "SystemInformation",
            operation: "testGetSystemInformation"
        };

        var testDoReboot = {
            type: "OperatingSystem",
            operation: "testDoReboot"
        };

        var testDoShutdown = {
            type: "OperatingSystem",
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

        $scope.doReboot = function () {
            $log.debug("Reboot operation selected");
        };

        $scope.doShutdown = function () {
            $log.debug("Shutdown operation selected");
        };

        $scope.getContent = function () {
            var options = {
                serviceName: "system",
                jsonMsg: testGetSystemInformation
            };

            serviceRequest(options, function(reply) {
                if ($scope.contents) $scope.contents.length = 0;
                $scope.contents = reply.data;
            });
        };

        // Service request, send the options (message) and the callback on completion
        function serviceRequest(options, scopeApplyCallback) {

            // We get back a promise
            serviceTabsService.sendRequest(options).then(function (reply) {
                var timer = $timeout(function () {
                    $scope.$apply(function () {
                        if (scopeApplyCallback) {
                            scopeApplyCallback(reply);
                        }
                    });
                }, 1000);

                timer.then(
                    function () { $log.debug("Timer resolved!", Date.now()); },
                    function () { $log.warn("Timer rejected!", Date.now()); }
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
        }

        $scope.getContent();
    }]);
