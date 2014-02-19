'use strict';
//vamiApp.lazy.factory('vertxEventBus', ['$rootScope', '$q', '$log', '$timeout', function ($rootScope, $q, $log, $timeout) {
vamiApp.factory('vertxEventBus', ['$rootScope', '$q', '$log', '$timeout', function ($rootScope, $q, $log, $timeout) {
    // This is work in progress for replacing the eb plugin
    var serverURL = location.protocol + '//' + location.hostname + ':' + (location.port || 80);
    var ebPath = '/eventbus';
    var reconnectEnabled = true;
    var reconnectInterval = 10000;
    var defaultTimeout = 5000;
    var eb = null;

    function closeEBConnection() {
        if (eb) {
            eb.close();
            //$scope.$digest();
        }
    }

    function openEBConnection(callback) {
        if (!eb) {
            var connectURL = serverURL + ebPath;
            $log.info("Creating EB, serverURL: " + connectURL);
            eb = new vertx.EventBus(connectURL);
            eb.onopen = function () {
                $log.debug("EventBus Connected");
                callback();
            };
            eb.onclose = function () {
                $log.debug("EventBus Closed");
                eb = null;
            };
        } else {
            callback();
        }
    }

    function realSend(address, jsonMsg) {
        $log.debug("sending address -> " + address + " msg ->" + jsonMsg);
        var deferred = $q.defer();

        try {
            eb.send(address, jsonMsg, function (reply) {
                $log.debug("response received: response -> " + reply);
                $log.debug("invoking provided cb with result: " + reply.result + " data: " + reply.data);

                var timer = $timeout(function() {
                    if (!reply.result === "ok") {
                        $log.error("Response was in error result: " + reply.result + " data: " + reply.data);
                        deferred.reject(reply);
                    } else {
                        deferred.resolve(reply);
                    }
                }, defaultTimeout);

                timer.then(
                    function () { $log.debug("Timer resolved!", Date.now()); },
                    function () { $log.warn("Timer rejected!", Date.now()); }
                );

                // Make sure to cleanup timers
               /* $rootScope.$on(
                    "$destroy",
                    function (event) {
                        if (timer) {
                            $timeout.cancel(timer);
                        }
                    }
                );*/
                //closeEBConnection();
            });
        } catch (e) {
            deferred.reject(e);
            $log.error("Failed to send to server: " + e.message);
        }

        return deferred.promise;

    }

    return {
        send: function (address, jsonMsg) {
            var promise = openEBConnection(function() {
                return realSend(address,jsonMsg);
            });

            return promise;
        }
    }
}]);
