'use strict';
/**
 * Scaffold App
 */
var @modName@App = angular.module('@modName@App', [
    'vamiCommon'
]).constant('APP_ROOT', $("#APP_ROOT").attr("href"))
    .constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href"))
    .constant('WEB_ROOT', $("#WEB_ROOT").attr("href"))
    .constant('SCRIPTS_ROOT', $("#SCRIPTS_ROOT").attr("href"))
    .constant('APP_DYNAMIC_RESOURCES', [
        {name: "tabsService.js", url: $("#APP_ROOT").attr("href") + "/services/tabs/tabsService.js"},
        {name: "tabsController.js", url: $("#APP_ROOT").attr("href") + "/controllers/tabs/tabsController.js"},
        {name: "@modName@TabController.js", url: $("#APP_ROOT").attr("href") + "/controllers/tabs/@modName@TabController.js"}
    ])
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', '$httpProvider', 'APP_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider, $injector, $routeProvider, $httpProvider, APP_ROOT) {

            @modName@App.lazy = {
                controller: $controllerProvider.register,
                directive: $compileProvider.directive,
                filter: $filterProvider.register,
                factory: $provide.factory,
                service: $provide.service,
                injector: $injector,
                httpProvider: $httpProvider
            };

            $routeProvider.when('/', {
                templateUrl: APP_ROOT + '/views/tabs/tabs.html',
                resolve: {
                    resources:function(resourceLoaderService, APP_DYNAMIC_RESOURCES) {
                        return resourceLoaderService.preloadJSResources(APP_DYNAMIC_RESOURCES);
                    }
                }
            }).otherwise({redirectTo: '/'});
        }
    ]);

/*
@modName@App.run(['$q', '$rootScope', '$log', 'resourceLoaderService', 'APP_DYNAMIC_RESOURCES',
    function ($q, $rootScope, $log, resourceLoaderService, APP_DYNAMIC_RESOURCES) {
        resourceLoaderService.preloadJSResources(APP_DYNAMIC_RESOURCES);
    }]);
*/

angular.element(document).ready(function () {
    angular.bootstrap(document.getElementById('@modName@Container'), ['@modName@App']);
});
