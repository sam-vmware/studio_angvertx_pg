'use strict';
/**
 * Scaffold App
 */
var @modName@App = angular.module('@modName@App', [
    'vamiCommon'
])
    .constant('APP_ROOT', $("#APP_ROOT").attr("href"))
    .constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href"))
    .constant('WEB_ROOT', $("#WEB_ROOT").attr("href"))
    .constant('SCRIPTS_ROOT', $("#SCRIPTS_ROOT").attr("href"))
    .constant('SYSTEM_DYNAMIC_RESOURCES', [
        {name: "@modName@Service.js", url: $("#APP_ROOT").attr("href") + "/services/@modName@Service.js"},
        {name: "@modName@Controller.js", url: $("#APP_ROOT").attr("href") + "/controllers/@modName@Controller.js"}
    ])
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', '$httpProvider', 'APP_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider, $injector, $routeProvider, $httpProvider, APP_ROOT) {

            systemApp.lazy = {
                controller: $controllerProvider.register,
                directive: $compileProvider.directive,
                filter: $filterProvider.register,
                factory: $provide.factory,
                service: $provide.service,
                injector: $injector,
                httpProvider: $httpProvider
            };

            $routeProvider.when('/', {
                templateUrl: APP_ROOT + '/views/@modName@View.html'
            }).otherwise({redirectTo: '/'});
        }
    ]);

systemApp.run(['$q', '$rootScope', '$log', 'resourceLoaderService', 'SYSTEM_DYNAMIC_RESOURCES',
    function ($q, $rootScope, $log, resourceLoaderService, SYSTEM_DYNAMIC_RESOURCES) {
        resourceLoaderService.preloadJSResources(SYSTEM_DYNAMIC_RESOURCES);
    }]);

angular.element(document).ready(function () {
    angular.bootstrap(document.getElementById('@modName@Container'), ['@modName@App']);
});
