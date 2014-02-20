'use strict';
//vamiApp.lazy.factory('vertxEventBus', ['$rootScope', '$q', '$log', '$timeout', function ($rootScope, $q, $log, $timeout) {
vamiApp.factory('vertxEventBus', ['$rootScope', '$q', '$log', '$timeout', function ($rootScope, $q, $log, $timeout) {
    // This is work in progress for replacing the eb plugin
    var serverURL = location.protocol + '//' + location.hostname + ':' + (location.port || 80);
    var ebPath = '/eventbus';
    var reconnectEnabled = true;
    var reconnectInterval = 10000;
    var eb = null;

    function closeEBConnection() {
        if (eb) {
            eb.close();
            //$scope.$digest();
        }
    }

    // Added cb since connection may not be fully opened
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

    function makeUID() { return _.uniqueId('vertxEventBus'); }
    function makeIDMsg(uid, msg) {
        return "id: " + uid + " " + msg;
    }

    function realSend(address, jsonMsg) {

        var transID = makeUID();
        $log.debug(makeIDMsg(transID, "sending address -> " + address + " msg ->" + jsonMsg));
        var deferred = $q.defer();
        try {
            openEBConnection(function () {
                eb.send(address, jsonMsg, function (reply) {
                    if (!reply.result === "ok") {
                        $log.error(makeIDMsg(transID, "Response was in error result: " + reply.result + " data: " + reply.data));
                        deferred.reject(reply);
                    } else {
                        deferred.resolve(reply);
                    }

                    $log.debug(makeIDMsg(transID, "response received: response -> " + reply));
                    if (!reply.result === "ok") {
                        $log.error(makeIDMsg(transID, "Response was in error result: " + reply.result + " data: " + reply.data));
                        deferred.reject(reply);
                    } else {
                        deferred.resolve(reply);
                    }

                });
            });
        } catch (e) {
            deferred.reject(e);
            $log.error(makeIDMsg(transID, "Failed to send to server: " + e.message));
        }

        return deferred.promise;
    }

    return {
        send: function (address, jsonMsg) {
            // Returns promise to make clear
            var promise = realSend(address, jsonMsg);
            return promise;
        }
    }
}]);
