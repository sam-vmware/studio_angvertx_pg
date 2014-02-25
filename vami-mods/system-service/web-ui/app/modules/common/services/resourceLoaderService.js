'use strict';
/**
 * Use this to fetch resources async
 * Created by samueldoyle.
 */
vamiApp.factory('resourceLoaderService', ['$rootScope', '$q', '$log', '$timeout', 'commonService',
    function ($rootScope, $q, $log, $timeout, commonService) {

        // Load JS remote resources, evalute them and insert into DOM, returns a promise
        function preloadJSResourcesReal ($rootScope, $q, $log, $timeout, commonService,
                                         appResources, resourcePostProcessCB) {
            jQuery.ajaxSetup({
                cache: true
            });
            var deferred = $q.defer();
            var promises = [];
            $.each(appResources, function (index, resource) {
                promises.push(_.loadRemoteJSFile(resource.url));
            });

            try {
                $q.all(promises).then(function (response, status) {
                    $.each(appResources, function (index, resource) {
                        console.debug('loaded: ' + resource.name);
                        if (resourcePostProcessCB) {
                            resourcePostProcessCB(resource);
                        }
                    });
                    $timeout(function () {
                        $rootScope.$apply();
                        $log.debug("All Resources loaded");
                        deferred.resolve();
                    }, 1000);
                }, function (e) {
                    deferred.reject(e);
                });
            } catch (error) {
                deferred.reject(error);
            }

            return deferred.promise;
        }

        return {
            // Returns a promise
            // appResources - array of maps of type e.g.
            // {name: "myJSFile.js", url: /jsfiles/myJSFile.js"},
            // resourcePostProcessCB (optional) - After each is retrieved and inserted into DOM this is called with the
            // the resource, so can cache for example
            preloadJSResources: function (appResources, resourcePostProcessCB) {
                // Returns promise to make clear
                var promise = preloadJSResourcesReal($rootScope, $q, $log, $timeout, commonService, appResources, resourcePostProcessCB);
                return promise;
            }
        }

}]);



