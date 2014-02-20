'use strict';
//vamiApp.lazy.factory('serviceTabsService', ['$q', '$log', 'vertxEventBus', function serviceTabsService($q, $log, vertxEventBus) {
vamiApp.factory('serviceTabsService', ['$q', '$log', 'vertxEventBus', function serviceTabsService($q, $log, vertxEventBus) {

    var SERVICE_INFO = {
        system: {
            address: "vami.SystemService"
        }
    };

    function fetchServerContent(options) {
        // Important! This returns a promise
        var svcName = options.serviceName;
        var msg = options.jsonMsg;

        var promise = vertxEventBus.send(SERVICE_INFO[svcName].address, msg);
        return promise;
    }

    return {
        sendRequest: function (options) {
            if (_.isUndefined(options.serviceName) || _.isUndefined(options.jsonMsg)) {
                throw new Error("Required serviceName or jsonMsg missing");
            }
            // returns promise
            var promise = fetchServerContent(options);
            return promise
        }
    }
}]);
