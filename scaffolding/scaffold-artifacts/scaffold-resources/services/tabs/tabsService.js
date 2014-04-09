'use strict';
/**
 * Created by samueldoyle
 */
@modName@App.lazy.factory('tabsService', ['$q', '$log', '$sce', 'vertxEventBus', 'APP_ROOT',
    function ($q, $log, $sce, vertxEventBus, APP_ROOT) {
        var ME = "tabsService";

        var SERVICE_INFO = {
            common: {
                newServiceAnnounceChannel: "vami.newServiceAnnounceChannel"
            },
            @serviceName@: {
                address: "@modName@.@serviceName@"
            }
        };

        var tabsList = [
            {
                title: "First Tab of @serviceName@", name: "@serviceName@", active: true, disabled: false,
                templ: $sce.trustAsResourceUrl(APP_ROOT + '/views/tabs/@modName@TabView.html')
            }
        ];

        function getTabs(options) {
            return tabsList;
        }

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
            },
            getTabs: function (options) {
                return getTabs(options)
            }
        }

    }]);
