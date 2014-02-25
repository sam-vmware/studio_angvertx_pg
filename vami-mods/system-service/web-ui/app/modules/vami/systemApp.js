'use strict';
/**
 * Created by samueldoyle
 */
var vamiApp = angular.module('systemApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ngGrid',
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
}]).constant('WEB_ROOT', $("#WEB_ROOT").attr("href"))
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', 'WEB_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider, $injector, $routeProvider, WEB_ROOT) {
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
                templateUrl: WEB_ROOT + '/modules/vami/views/tabs/systemTab.html'
            }).otherwise({redirectTo: '/'});
        }
    ]);

// Start things off, transition to index
/*vamiApp.run(['$log',
 function ($log) {
 $log.debug("Inside vamiApp.run");
 }
 ]);*/
