'use strict';
/**
 * Created by samueldoyle
 */
vamiApp.lazy.factory('serviceTabsService', ['$q', '$log', '$sce', '$rootScope', '$timeout', 'vertxEventBus', 'VAMI_ROOT',
    function serviceTabsService($q, $log, $sce, $rootScope, $timeout, vertxEventBus, VAMI_ROOT) {
//vamiApp.factory('serviceTabsService', ['$q', '$log', 'vertxEventBus', function serviceTabsService($q, $log, vertxEventBus) {
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
                title: "VM", name: "vm", active: false,  disabled: false,
                templ: $sce.trustAsResourceUrl(VAMI_ROOT + '/views/tabs/vmTab.html')
            },
            {
                title: "vApp", name: "vapp", active: false, disabled: true,
                templ: $sce.trustAsResourceUrl(VAMI_ROOT + '/views/tabs/vApp.html')
            },
            // { title: "System", name: "system", state: "serviceTabs.system", active: true, disabled: false, templ: VAMI_ROOT + '/views/tabs/systemTab.html'},
            {
                title: "Network", name: "network", active: false, disabled: false,
                templ: $sce.trustAsResourceUrl(VAMI_ROOT + '/views/tabs/networkTab.html')
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
                $log.info(ME + " Retrieved service info: " + response.data);
            }, function (e) {
                $log.error(ME + " Failed to make request for all existing services: " + e.message);
            })
        } catch (error) {
            $log.error(ME + " Failed to make request for all existing services: " + error.message);
        }

        // Subscribe to global new service channel
        vertxEventBus.subscribe(SERVICE_INFO.common.newServiceAnnounceChannel, function (announceMsg) {
            $log.debug(ME + " Received new service announce message");
            var data = announceMsg.data;
            var newTab = {
                title: data.title, name: data.svcName, active: false, disabled: data.isDisabled,
                templ: $sce.trustAsResourceUrl(data.webRootDir + "/" + data.indexFile)
            };
            $log.debug(ME + "indexFile -> " + data.indexFile + " svcName -> " + data.svcName + " title -> "
                       + data.title + " webRootDir -> " + data.webRootDir + " indexFile -> " + data.indexFile);
            $timeout(function() {
                $rootScope.apply(function() {
                    tabsList.push(newTab);
                });
            }, 2000);
        });

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
