'use strict';
/**
 * Created by samueldoyle
 */
var vamiApp = angular.module('vamiApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ngGrid',
    'ui.router',
    'template/tabs/tab.html',
    'template/tabs/tabset.html',
    'ui.bootstrap.tabs',
    'ui.bootstrap.buttons',
    'ui.bootstrap.transition',
    'template/modal/backdrop.html',
    'template/modal/window.html',
    'ui.bootstrap.modal'
]).factory('commonService', ['$resource', '$cacheFactory', function ($resource, $cacheFactory) {
    var cache = $cacheFactory('commonService');
    var loadedSuffix = "_isLoaded";
    return {
        getResourceLoadedFlag: function (id) {
            var retVal = false;
            id += loadedSuffix;
            var isLoaded = cache.get(id);
            if (isLoaded) {
                retVal = true;
            }
            return retVal;
        },
        setResourceLoadedFlag: function (id) {
            id += loadedSuffix;
            cache.put(id, true);
        },
        clearResourceLoadedFlag: function (id) {
            id += loadedSuffix;
            cache.remove(id);
        }
    };
}]).constant('MAIN_ROOT', $("#MAIN_ROOT").attr("href")).constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href")).constant('VAMI_ROOT', $("#VAMI_ROOT").attr("href"))
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$stateProvider',
        '$urlRouterProvider', '$sceProvider', '$rootScopeProvider', '$locationProvider', '$injector', 'COMMON_ROOT', 'VAMI_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $stateProvider, $urlRouterProvider, $sceProvider, $rootScopeProvider,
                  $locationProvider, $injector, COMMON_ROOT, VAMI_ROOT) {
            $sceProvider.enabled(false); // dealing with max digest attempts
            $rootScopeProvider.digestTtl(10); // dealing with max digest attempts
            $urlRouterProvider.otherwise("/index/services");

            $provide.decorator('$exceptionHandler', function ($delegate) {
                return function (exception, cause) {
                    $delegate(exception, cause);
                    var trace = printStackTrace({e: exception});
                    console.debug("Trace: " + trace.join('\n'));
                };
            });

            // Remote fetching, I don't see a better of doing this besides using the $.getScript but it generates
            // a lot of anguarl max iteration blah .. errors
            var appResources = [
                {name: "confirmDialogController.js", url: COMMON_ROOT + "/controllers/dialog/confirmDialogController.js"},
                {name: "serviceTabsService.js", url: VAMI_ROOT + "/services/tabs/serviceTabsService.js"},
                {name: "serviceTabsController.js", url: VAMI_ROOT + "/controllers/tabs/serviceTabsController.js"},
                {name: "systemTabController.js", url: VAMI_ROOT + "/controllers/tabs/systemTabController.js"}
            ];

            vamiApp.lazy = {
                controller: $controllerProvider.register,
                directive: $compileProvider.directive,
                filter: $filterProvider.register,
                factory: $provide.factory,
                service: $provide.service,
                injector: $injector
            };

            var serviceTabs = {
                name: 'serviceTabs',
                abstract: true,
                url: '',
                template: "<div ui-view></div>",
                reloadOnSearch: false,
                access: { isPrivate: 0 },
                resolve: {
                    // each function in this resolve block witll be resolved and injected into the controller
                    // if we return a promise, like here, it will be resolved first
                    preloadedResources: ['$q', '$timeout', '$rootScope', '$log', 'commonService', function ($q, $timeout, $rootScope, $log, commonService) {
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
                                    console.log('loaded: ' + resource.name);
                                    //commonService.setResourceLoadedFlag(resource.name);
                                });
                                $timeout(function () {
                                    $rootScope.$apply();
                                    $log.debug("All Resources loaded");
                                    deferred.resolve();
                                }, 1000);
                            }, function (e) {
                                deferred.reject(e.message);
                            });
                        } catch (error) {
                            deferred.reject(error.message);
                        }

                        return deferred.promise;
                    }]
                },
                views: {
                    tabs: {
                        templateUrl: VAMI_ROOT + '/views/tabs/serviceTabs.html'
                    }
                }
            };

            var vmTab = {
                name: 'serviceTabs.vm',
                url: "/vm",
                access: { isPrivate: 0 },
                reloadOnSearch: false,
                views: {
                    tabContent: {
                        templateUrl: VAMI_ROOT + '/views/tabs/vmTab.html'
                    }
                }
            };

            var vappTab = {
                name: 'serviceTabs.vapp',
                url: "/vapp",
                access: { isPrivate: 0 },
                reloadOnSearch: false,
                views: {
                    tabContent: {
                        templateUrl: VAMI_ROOT + '/views/tabs/vApp.html'
                    }
                }
            };

            var systemTab = {
                name: 'serviceTabs.system',
                url: "/system",
                access: { isPrivate: 0 },
                reloadOnSearch: true,
                views: {
                    tabContent: {
                        templateUrl: VAMI_ROOT + '/views/tabs/systemTab.html'
                    }
                }
            };

            var networkTab = {
                name: 'serviceTabs.network',
                url: "/network",
                access: { isPrivate: 0 },
                reloadOnSearch: false,
                views: {
                    tabContent: {
                        templateUrl: VAMI_ROOT + '/views/tabs/networkTab.html'
                    }
                }
            };

            $stateProvider.state(serviceTabs);
            $stateProvider.state(vmTab);
            $stateProvider.state(vappTab);
            $stateProvider.state(systemTab);
            $stateProvider.state(networkTab);
        }
    ]);

// Start things off, transition to index
vamiApp.run(["$state", function ($state) {
    $state.go("serviceTabs.system");
}]);
