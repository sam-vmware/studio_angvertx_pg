'use strict';
/**
 * Created by samueldoyle
 */
vamiApp.controller('systemTabController', ['$q', '$scope', '$routeParams', '$log', '$timeout', '$modal',
    'WEB_ROOT', 'serviceTabsService',
    function ($q, $scope, $routeParams, $log, $timeout, $modal, WEB_ROOT, serviceTabsService) {

        var REBOOT_OP = "Reboot Operation";
        var SHUTDOWN_OP = "Shutdown Operation";

        $scope.open = function (modalData, confirmCB, rejectedCB) {
            var modalInstance = $modal.open({
                templateUrl: WEB_ROOT + '/modules/common/views/dialog/confirmDialog.html',
                controller: 'confirmDialogController',
                resolve: {
                    modalData: function () {
                        return modalData;
//                        return $scope.modalData;
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                // promise resolved in the dialog i.e. comfirmed
                //$scope.selected = selectedItem;
                confirmCB();
            }, function () {
                // promise rejected in the dialog i.e. cancel
                rejectedCB();
            });
        };

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

            var opConfirmedCB = function () {
                $log.debug("Shutdown operation confirmed sending request ...");
                serviceRequest({
                    serviceName: "system",
                    jsonMsg: testDoReboot
                }, function (reply) {
                    $log.debug("Shutdown request response received");
                    if (reply.data) {
                        $log.debug("reply.data -> " + reply.data);
                    }
                });
            };

            var opRejectedCB = function () {
                $log.debug("Shutdown operation rejected, request will not be sent");
            };

            $scope.open({confirmText:REBOOT_OP}, opConfirmedCB, opRejectedCB);
        };

        $scope.doShutdown = function () {
            $log.debug("Shutdown operation selected");
            var opConfirmedCB = function () {
                $log.debug("Shutdown operation confirmed sending request ...");
                serviceRequest({
                    serviceName: "system",
                    jsonMsg: testDoShutdown
                }, function (reply) {
                    $log.debug("Shutdown request response received");
                    if (reply.data) {
                        $log.debug("reply.data -> " + reply.data);
                    }
                });
            };
            var opRejectedCB = function () {
                $log.debug("Shutdown operation rejected, request will not be sent");
            };

            $scope.open({confirmText:SHUTDOWN_OP}, opConfirmedCB, opRejectedCB);
        };

        // This makes the request through the service layer which ultimately uses the eventbus
        // It is p2p so will wait on response with potential timeout
        $scope.getContent = function () {
            serviceRequest({
                serviceName: "system",
                jsonMsg: testGetSystemInformation
            }, function (reply) {
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
        }

        $scope.getContent();
    }]);
