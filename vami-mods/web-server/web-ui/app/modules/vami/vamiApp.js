'use strict';
/**
 * Created by samueldoyle
 */
var vamiApp = angular.module('vamiApp', ['vamiCommon']);
vamiApp.constant('MAIN_ROOT', $("#MAIN_ROOT").attr("href"))
    .constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href"))
    .constant('VAMI_ROOT', $("#VAMI_ROOT").attr("href"))
    .constant('DYNAMIC_RESOURCES', [
        {name: "serviceTabsService.js", url: $("#VAMI_ROOT").attr("href") + "/services/tabs/serviceTabsService.js"},
        {name: "serviceTabsController.js", url: $("#VAMI_ROOT").attr("href") + "/controllers/tabs/serviceTabsController.js"},
    ])
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', 'DYNAMIC_RESOURCES', 'COMMON_ROOT', 'VAMI_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider, $injector, $routeProvider, DYNAMIC_RESOURCES, COMMON_ROOT, VAMI_ROOT) {
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
    }]);

vamiApp.run(['$q', '$rootScope', '$log', 'resourceLoaderService', 'DYNAMIC_RESOURCES',
    function ($q, $rootScope, $log, resourceLoaderService, DYNAMIC_RESOURCES) {
        resourceLoaderService.preloadJSResources(DYNAMIC_RESOURCES);
    }]);
