'use strict';

var vamiApp = angular.module('vamiApp', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute',
    'ui.bootstrap',
    'ui.router',
]);

vamiApp.factory('commonService', ['$resource', '$cacheFactory', function($resource, $cacheFactory) {
    var cache = $cacheFactory('commonService');
    var loadedSuffix= "_isLoaded";
    return {
        getResourceLoadedFlag: function(id) {
            var retVal = false;
            id += loadedSuffix;
            var isLoaded = cache.get(id);
            if (isLoaded) {
                retVal = true;
            }
            return retVal;
        },
        setResourceLoadedFlag: function(id) {
            id += loadedSuffix;
            cache.put(id, true);
        },
        clearResourceLoadedFlag: function(id) {
            id += loadedSuffix;
            cache.remove(id);
        }
    };
}]);

vamiApp
    .constant('VAMI_ROOT', $("#VAMI_ROOT").attr("href"))
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$stateProvider',
        '$urlRouterProvider', '$sceProvider', '$rootScopeProvider', 'VAMI_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $stateProvider,
                  $urlRouterProvider, $sceProvider, $rootScopeProvider, VAMI_ROOT) {
            $sceProvider.enabled(false);
            $rootScopeProvider.digestTtl(10);

            vamiApp.lazy = {
                controller: $controllerProvider.register,
                directive: $compileProvider.directive,
                filter: $filterProvider.register,
                factory: $provide.factory,
                service: $provide.service
            };

            var vamiroot = {
                abstract: true,
                name: 'vamiroot',
                url: '/vami',
                template: "<div ui-view></div>",
                resolve: {
                    t1: ['$q', 'commonService', function ($q, commonService) {
                        var resourceName = "serviceTabsService";
                        if (commonService.getResourceLoadedFlag(resourceName)) {
                            console.info(resourceName + ' already loaded ...');
                            return;
                        }
                        return $q.when($.getScript(VAMI_ROOT + '/services/tabs/serviceTabsService.js')).then(
                            function (response, status) {
                                console.info(resourceName + ' loaded, status: ' + status);
                                commonService.setResourceLoadedFlag(resourceName);
                            });
                    }],
                    t2: ['$q', 'commonService', function ($q, commonService) {
                        var resourceName = "serviceTabsController";
                        if (commonService.getResourceLoadedFlag(resourceName)) {
                            console.info(resourceName + ' already loaded ...');
                            return;
                        }
                        return $q.when($.getScript(VAMI_ROOT + '/controllers/tabs/serviceTabsController.js')).then(
                            function (response, status) {
                                console.info(resourceName + ' loaded, status: ' + status);
                                commonService.setResourceLoadedFlag(resourceName);
                            });
                    }]
                }
            };

            var vamiindex = {
                name: 'vamiroot.index',
                url: '/index',
                templateUrl: VAMI_ROOT + '/views/vamiApp.html'
            };

            $stateProvider.state(vamiroot);
            $stateProvider.state(vamiindex);
        }
    ]);

vamiApp.run(['$state', function ($state) {
    $state.transitionTo('vamiroot.index');
}]);
