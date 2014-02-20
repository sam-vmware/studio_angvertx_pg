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

        // Do reboot and shutdown send test request, careful not to send the real one
        $scope.doReboot = function () {
            $log.debug("Shutdown operation selected");
            serviceRequest({
                serviceName: "system",
                jsonMsg: testDoReboot
            }, function(reply) {
                $log.debug("Shutdown request response received");
                if (reply.data) {
                    $log.debug("reply.data -> " + reply.data);
                }
            });
        };

        $scope.doShutdown = function () {
            $log.debug("Shutdown operation selected");
            serviceRequest({
                serviceName: "system",
                jsonMsg: testDoShutdown
            }, function(reply) {
                $log.debug("Shutdown request response received");
                if (reply.data) {
                    $log.debug("reply.data -> " + reply.data);
                }
            });
        };

        // This makes the request through the service layer which ultimately uses the eventbus
        // It is p2p so will wait on response with potential timeout
        $scope.getContent = function () {
            serviceRequest({
                serviceName: "system",
                jsonMsg: testGetSystemInformation
            }, function(reply) {
                if ($scope.contents) $scope.contents.length = 0;
                $scope.contents = reply.data;
            });
        };

        // Service request, send the options (message) and the callback on completion
        function serviceRequest(options, scopeApplyCallback) {

            // We get back a promise
            // Options must be of form e.g.
            // {
            //  serviceName: "system",
            //  jsonMsg: testGetSystemInformation
            // }
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
