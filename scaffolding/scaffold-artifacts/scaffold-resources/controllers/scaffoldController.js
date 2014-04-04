'use strict';
/**
 * Scaffold Controller
 */
@modName@App.lazy.controller('@modName@Controller', ['$q', '$scope', '$routeParams', '$log', '$timeout', '$modal',
    'COMMON_ROOT', '@modName@Service',
    function ($q, $scope, $routeParams, $log, $timeout, $modal, COMMON_ROOT, @modName@Service) {

        var getMessage = {
            type: "HelloWorld",
            operation: "sayHello"
        };

        $scope.contents = [];

        // This makes the request through the service layer which ultimately uses the eventbus
        // It is p2p so will wait on response with potential timeout
        $scope.getContent = function () {
            serviceRequest({
                serviceName: "@serviceName@",
                jsonMsg: getMessage
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
            @modName@Service.sendRequest(options).then(function (reply) {
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
