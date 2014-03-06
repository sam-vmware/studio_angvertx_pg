'use strict';
/**
 * Created by samueldoyle
 */
vamiApp.factory('serviceTabsService', ['$q', '$log', 'vertxEventBus', 'WEB_ROOT',
    function serviceTabsService($q, $log, vertxEventBus, WEB_ROOT) {
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
            }
        }
    }]);