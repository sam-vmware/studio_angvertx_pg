'use strict';
/**
 * Created by samueldoyle
 */
systemApp.lazy.factory('systemTabsService', ['$q', '$log', '$sce', 'vertxEventBus', 'VAMI_ROOT',
    function systemTabsService($q, $log, $sce, vertxEventBus, VAMI_ROOT) {
        var ME = "serviceTabsService";

        var SERVICE_INFO = {
            common: {
                newServiceAnnounceChannel: "vami.newServiceAnnounceChannel"
            },
            contentResolver: {
                address: "vami.ContentResolverService",
                messages: {
                    getAllServicesMessage: {
                        type: "ResourceRequestHandler",
                        operation: "getServices"
                    }
                }
            },
            system: {
                address: "vami.SystemService"
            }
        };

        var tabsList = [
            {
                title: "Information", name: "information", active: true, disabled: false,
                templ: $sce.trustAsResourceUrl(VAMI_ROOT + '/views/tabs/informationTab.html')
            },
            {
                title: "Time Zone", name: "timeZone", active: false, disabled: false,
                templ: $sce.trustAsResourceUrl(VAMI_ROOT + '/views/tabs/timeZoneTab.html')
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

        // Fetch existing services
        try {
            fetchServerContent({
                serviceName: "contentResolver",
                jsonMsg: SERVICE_INFO.contentResolver.messages.getAllServicesMessage
            }).then(function (response) {
                $log.debug(ME + " Retrieved service info: " + JSON.stringify(response.data));
            }, function (e) {
                $log.error(ME + " Failed to make request for all existing services: " + e.message);
            })
        } catch (error) {
            $log.error(ME + " Failed to make request for all existing services: " + error.message);
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
