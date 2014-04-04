'use strict';
/**
 * Scaffold Service
 */
@modName@App.lazy.factory('@modName@Service', ['$q', '$log', '$sce', 'vertxEventBus', 'APP_ROOT',
    function ($q, $log, $sce, vertxEventBus, APP_ROOT) {
        var ME = "@modName@Service";

        var SERVICE_INFO = {
            common: {
                newServiceAnnounceChannel: "vami.newServiceAnnounceChannel"
            },
            @serviceName@: {
                address: "@modName@.@serviceName@"
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
