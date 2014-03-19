'use strict';
/**
 * Created by samueldoyle
 * This is an angularservice to wrap the vertx eventbus
 */
vamiCommon.lazy.factory('vertxEventBus', ['$rootScope', '$q', '$log', '$timeout', '$cookieStore', '$cookies', 'eventServices', 'VERTX_SEND_EVENT', 'VERTX_RESPONSE_EVENT',
    function ($rootScope, $q, $log, $timeout, $cookieStore, $cookies, eventServices, VERTX_SEND_EVENT, VERTX_RESPONSE_EVENT) {
        // This is work in progress for replacing the eb plugin
        var serverURL = location.protocol + '//' + location.hostname + ':' + (location.port || 80);
        var ebPath = '/eventbus';
        var reconnectEnabled = true;
        var reconnectInterval = 10000;
        var sessionCookieKEY = "sessionID" ; // for linking eventbus authentication with what we set on PAM auth
        var eb = null;

        function closeEBConnection() {
            if (eb) {
                eb.close();
                eb = null;
                //$scope.$digest();
            }
        }

        // Added cb since connection may not be fully opened
        function openEBConnection(options, callback) {
            options = options || {};
            if (!eb) {
                var connectURL = serverURL + ebPath;
                $log.info("Creating EB, serverURL: " + connectURL);
                eb = new vertx.EventBus(connectURL);
                if (options.sessionID) {
                   eb.sessionID = options.sessionID;
                }
                eb.onopen = function () {
                    $log.debug("EventBus Connected");
                };
                eb.onclose = function () {
                    $log.debug("EventBus Closed");
                    eb = null;
                };
            }

            if (eb.readyState() == vertx.EventBus.OPEN) {
                callback();
                return;
            }

            if (eb.readyState() != vertx.EventBus.OPEN) {
                $timeout(function () {
                    callback();
                }, 1000);
            } else {
                callback();
            }

        }

        /**
         * Way of tracking messages using a uid generator
         * @returns {*}
         */
        function makeUID() {
            return _.uniqueId('vertxEventBus');
        }

        function makeIDMsg(uid, msg) {
            return "id: " + uid + " " + msg;
        }

        function addEBSessionID(options) {
            options = options || {};
            var sessionIDFromCookie = $cookies.sessionID;
            return _.extend({
                sessionID: sessionIDFromCookie
            }, options);
        }

        /**
         * Sends a message point to point
         * @param address - The eventbus address
         * @param jsonMsg - the Mesage received
         * @returns {*} - returns a promise
         */
        function send(address, jsonMsg) {
            var transID = makeUID();
            $log.debug(makeIDMsg(transID, "sending address -> " + address + " msg ->" + JSON.stringify(jsonMsg)));
            var deferred = $q.defer();
            try {
                var options = addEBSessionID();
                $log.debug("sessionID from Cookie Value: " + options.sessionID);
                openEBConnection(options, function () {
                    eventServices.broadCastEvent(VERTX_SEND_EVENT);
                    eb.send(address, jsonMsg, function (reply) {
                        if (!reply.result === "ok") {
                            $log.error(makeIDMsg(transID, "Response was in error result: " + reply.result + " data: " + JSON.stringify(reply.data)));
                            deferred.reject(reply);
                        } else {
                            deferred.resolve(reply);
                        }

                        $log.debug(makeIDMsg(transID, "response received: response -> " + JSON.stringify(reply)));
                        if (!reply.result === "ok") {
                            $log.error(makeIDMsg(transID, "Response was in error result: " + reply.result + " data: " + JSON.stringify(reply.data)));
                            deferred.reject(reply);
                        } else {
                            deferred.resolve(reply);
                        }
                        eventServices.broadCastEvent(VERTX_RESPONSE_EVENT);
                    });
                });
            } catch (e) {
                deferred.reject(e);
                $log.error(makeIDMsg(transID, "Failed to send to server: " + e.message));
                eventServices.broadCastEvent(VERTX_RESPONSE_EVENT);
            }

            return deferred.promise;
        }

        /**
         * @param address - topic address to subscribe
         * @param msgReceivedCB - callback invoked with a message when received
         * @returns {*} - returns a promise
         */
        function subscribe(address, msgReceivedCB) {
            var transID = makeUID();
            $log.debug(makeIDMsg(transID, "subscribing to address -> " + address));
            var deferred = $q.defer();
            try {
                var options = addEBSessionID();
                $log.debug("sessionID from Cookie Value: " + options.sessionID);
                openEBConnection(options, function () {
                    eb.registerHandler(address, msgReceivedCB);
                    deferred.resolve();
                });
            } catch (e) {
                deferred.reject(e);
                $log.error(makeIDMsg(transID, "Failed to subscribe to address: " + address + " " + e.message));
            }

            return deferred.promise;
        }

        return {
            // This is point to point and returns promise
            send: function (address, jsonMsg) {
                // Returns promise to make clear
                var promise = send(address, jsonMsg);
                return promise;
            },
            // Subscribe to some topic, call back invoked on message
            subscribe: function (address, msgReceivedCB) {
                // Returns promise to make clear
                var promise = subscribe(address, msgReceivedCB);
                return promise;
            }
        }
    }]);
