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
    'ui.bootstrap.modal',
    'http-auth-interceptor'
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
            $locationProvider.html5Mode(true);
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
    ])
    .controller('loginController', ['$scope', '$http', function loginController($scope, $http) {
        $scope.formData = {};
        $scope.login = function () {
            $http({
                method: 'POST',
                url: '/auth/login',
                data: $.param($scope.formData),
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            }).success(function (data, status) {
                if (data == 'true') {
                    $scope.message = data.message;
                    authService.loginConfirmed();
                } else {
                    alert('Wrong username or password.');
                    $scope.message = data.message;
                    $scope.errorName = data.errors.name;
                }
            });
        };
    }])
    .directive("mainApplication", function () {
        return {
            restrict: "C",
            link: function (scope, elem, attrs) {
                elem.removeClass("loading-spinner");

                var login = elem.find("#login-container");
                var main = elem.find("#vami-app-container");

                login.hide();

                scope.$on("event:auth-loginRequired", function () {
                    login.slideDown("slow", function () {
                        main.hide();
                    });
                });
                scope.$on("event:auth-loginConfirmed", function () {
                    main.show();
                    login.slideUp();
                });
            }
        }
    })
    .run(['$rootScope', function($rootScope) {
        $rootScope.logout = function () {
            window.location = '/auth/logout';
        };
    }]);

// Start things off, transition to index
/*vamiApp.run(['$log',
 function ($log) {
 $log.debug("Inside vamiApp.run");
 }
 ]);*/
