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
        return vertxEventBus.send(SERVICE_INFO[svcName].address, msg);
    }

    return {
        getTabContent: function (options) {
            // returns promise
            if (_.isUndefined(options.serviceName) || _.isUndefined(options.jsonMsg)) {
                throw new Error("Required serviceName or jsonMsg missing");
            }
            return fetchServerContent(options);
        }
    }
}]);
