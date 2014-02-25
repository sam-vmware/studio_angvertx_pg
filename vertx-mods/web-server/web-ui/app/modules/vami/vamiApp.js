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
        },
        clearAllResources: function () {
            cache.removeAll();
        }
    };
}]).constant('MAIN_ROOT', $("#MAIN_ROOT").attr("href"))
    .constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href"))
    .constant('VAMI_ROOT', $("#VAMI_ROOT").attr("href"))
    .constant('DYNAMIC_RESOURCES', [
        {name: "confirmDialogController.js", url: $("#COMMON_ROOT").attr("href") + "/controllers/dialog/confirmDialogController.js"},
        {name: "serviceTabsService.js", url: $("#VAMI_ROOT").attr("href") + "/services/tabs/serviceTabsService.js"},
        {name: "serviceTabsController.js", url: $("#VAMI_ROOT").attr("href") + "/controllers/tabs/serviceTabsController.js"},
    ])
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', 'DYNAMIC_RESOURCES', 'COMMON_ROOT', 'VAMI_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider,
                  $injector, $routeProvider, DYNAMIC_RESOURCES, COMMON_ROOT, VAMI_ROOT ) {
            $sceProvider.enabled(false); // dealing with max digest attempts
            $rootScopeProvider.digestTtl(10); // dealing with max digest attempts
            $provide.decorator('$exceptionHandler', function ($delegate) {
                return function (exception, cause) {
                    $delegate(exception, cause);
                    var trace = printStackTrace({e: exception});
                    console.debug("Trace: " + trace.join('\n'));
                };
            });

           vamiApp.lazy = {
                controller: $controllerProvider.register,
                directive: $compileProvider.directive,
                filter: $filterProvider.register,
                factory: $provide.factory,
                service: $provide.service,
                injector: $injector
            };

            $routeProvider.when('/', {
                templateUrl: VAMI_ROOT + '/views/tabs/serviceTabs.html',
                resolve: {
                    deps: ['$q', '$log', '$rootScope', 'resourceLoaderService',
                        function ($q, $log, $rootScope, resourceLoaderService) {
                            return resourceLoaderService.preloadJSResources(DYNAMIC_RESOURCES)
                        }]
                }
            }).otherwise({redirectTo: '/'});
        }
    ]);

// Start things off, transition to index
/*vamiApp.run(['$log',
 function ($log) {
 $log.debug("Inside vamiApp.run");
 }
 ]);*/
