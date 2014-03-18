'use strict';
/**
 * Created by samueldoyle
 * Place to add event based support
 */
vamiCommon.lazy.factory('eventServices', ['$rootScope', '$q', '$log', '$timeout',
    function ($rootScope, $q, $log, $timeout) {
        function broadCastEvent($rootScope, $q, $log, $timeout, event, data) {
            $log.debug("eventServices broadCasting event: " + event)
            $rootScope.$broadcast(event, data);
        }

        function registerForEvent($rootScope, $q, $log, $timeout, event, listener) {
            $rootScope.$on(event, listener);
        }

        return {
            broadCastEvent: function (event, data) {
                broadCastEvent($rootScope, $q, $log, $timeout, event, data);
            },
            registerForEvent: function (event, listener) {
                registerForEvent($rootScope, $q, $log, $timeout, event, listener);
            }
        }
    }
]);
