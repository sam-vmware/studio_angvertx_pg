'use strict';
/**
 * Created by samueldoyle
 */
var systemApp = angular.module('systemApp', [
    'vamiCommon'
])
    .constant('VAMI_ROOT', $("#VAMI_ROOT").attr("href"))
    .constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href"))
    .constant('WEB_ROOT', $("#WEB_ROOT").attr("href"))
    .constant('SCRIPTS_ROOT', $("#SCRIPTS_ROOT").attr("href"))
    .constant('SYSTEM_DYNAMIC_RESOURCES', [
        {name: "systemTabsService.js", url: $("#VAMI_ROOT").attr("href") + "/services/tabs/systemTabsService.js"},
        {name: "systemTabsController.js", url: $("#VAMI_ROOT").attr("href") + "/controllers/tabs/systemTabsController.js"},
        {name: "informationTabController.js", url: $("#VAMI_ROOT").attr("href") + "/controllers/tabs/informationTabController.js"},
        {name: "timeZoneController.js", url: $("#VAMI_ROOT").attr("href") + "/controllers/tabs/timeZoneController.js"}
    ])
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', '$httpProvider', 'VAMI_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider, $injector, $routeProvider, $httpProvider, VAMI_ROOT) {

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
                templateUrl: VAMI_ROOT + '/views/tabs/systemTabs.html',
                resolve: {
                    resources:function(resourceLoaderService, SYSTEM_DYNAMIC_RESOURCES) {
                        return resourceLoaderService.preloadJSResources(SYSTEM_DYNAMIC_RESOURCES);
                    }
                }
            }).otherwise({redirectTo: '/'});
        }
    ]);

/*systemApp.run(['$q', '$rootScope', '$log', 'resourceLoaderService', 'SYSTEM_DYNAMIC_RESOURCES',
    function ($q, $rootScope, $log, resourceLoaderService, SYSTEM_DYNAMIC_RESOURCES) {
        resourceLoaderService.preloadJSResources(SYSTEM_DYNAMIC_RESOURCES);
    }]);*/

angular.element(document).ready(function () {
    angular.bootstrap(document.getElementById('systemAppContainer'), ['systemApp']);
});
