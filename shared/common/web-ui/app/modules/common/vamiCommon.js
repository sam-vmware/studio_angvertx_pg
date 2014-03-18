'use strict';
/**
 * Common module for dealing with event broadcasting
 * Created by samueldoyle.
 */
    // Nice suggestion for dealing with getScript limitations here:
    // http://www.lockencreations.com/2011/07/02/cant-debug-imported-js-files-when-using-jquery-getscript/

    // Add to underscore via mixin and extend the solution with a promise
_.mixin({
    loadRemoteJSFile: function (url, callback) {
        var deferred = $.Deferred();

        deferred.done(function (value) {
            if (callback) {
                callback(url);
            }
        });

        var head = document.getElementsByTagName("head")[0];
        var script = document.createElement("script");
        script.src = url;

        // Handle Script loading
        var done = false;

        // Attach handlers for all browsers
        script.onload = script.onreadystatechange = function () {
            if (!done && (!this.readyState || this.readyState == "loaded" || this.readyState == "complete")) {
                done = true;
                deferred.resolve();

                // Handle memory leak in IE
                script.onload = script.onreadystatechange = null;
            }
        };
        head.appendChild(script);

        // We handle everything using the script element injection
        return deferred.promise();
    }
});

_.mixin({
    arrayObjectIndexOf: function (array, value, property) {
        for (var i = 0, len = array.length; i < len; i++) {
            if (array[i][property] === value) return i;
        }
        return -1;
    }
});

angular.module("template/tabs/tab.html", []).run(["$templateCache", function ($templateCache) {
    $templateCache.put("template/tabs/tab.html",
            '<li ng-class="{active: active, disabled: disabled}">' +
            '    <a ng-click="select()" tab-heading-transclude>{{heading}}</a>' +
            '</li>');
}]);

angular.module("template/tabs/tabset.html", []).run(["$templateCache", function ($templateCache) {
    $templateCache.put("template/tabs/tabset.html",
            '<div>' +
            '   <ul class="nav nav-{{type || \'tabs\'}}" ng-class="{\'nav-stacked\': vertical, \'nav-justified\': justified}" ng-transclude></ul>' +
            '    <div class="tab-content">' +
            '        <div class="tab-pane" ' +
            '           ng-repeat="tab in tabs" ' +
            '           ng-class="{active: tab.active}"' +
            '           tab-content-transclude="tab">' +
            '       </div>' +
            '    </div>' +
            '</div>');
}]);

angular.module("template/modal/backdrop.html", []).run(["$templateCache", function ($templateCache) {
    $templateCache.put("template/modal/backdrop.html",
            '<div class="modal-backdrop fade"' +
            '   ng-class="{in: animate}"' +
            '   ng-style="{\'z-index\': 1040 + (index && 1 || 0) + index*10}"' +
            '></div>'
    );
}]);

angular.module("template/modal/window.html", []).run(["$templateCache", function ($templateCache) {
    $templateCache.put("template/modal/window.html",
            '<div tabindex="-1" class="modal fade {{ windowClass }}" ng-class="{in: animate}" ng-style="{\'z-index\': 1050 + index*10, display: \'block\'}" ng-click="close($event)">' +
            '   <div class="modal-dialog"><div class="modal-content" ng-transclude></div> ' +
            '</div>' +
            '</div>'
    );
}]);

var vamiCommon = angular.module('vamiCommon', [
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
]).constant('VERTX_SEND_EVENT', 'vertxEventBus:request:send')
    .constant('VERTX_RESPONSE_EVENT', 'vertxEventBus:response:received')
    .constant('COMMON_ROOT', $("#COMMON_ROOT").attr("href"))
    .constant('SCRIPTS_ROOT', $("#SCRIPTS_ROOT").attr("href"))
    .constant('COMMON_DYNAMIC_RESOURCES', [
        {name: "sockjs-0.3.min.js", url: $("#SCRIPTS_ROOT").attr("href") + "/sockjs-0.3.min.js"},
        {name: "vertxbus-2.1.js", url: $("#SCRIPTS_ROOT").attr("href") + "/vertxbus-2.1.js"},
        {name: "stacktrace.js", url: $("#SCRIPTS_ROOT").attr("href") + "/stacktrace.js"},
        {name: "confirmDialogController.js", url: $("#COMMON_ROOT").attr("href") + "/controllers/dialog/confirmDialogController.js"},
//        {name: "commonBodyDirective.js", url: $("#COMMON_ROOT").attr("href") + "/directives/commonBodyDirective.js"},
        {name: "cacheService.js", url: $("#COMMON_ROOT").attr("href") + "/services/cacheService.js"},
        {name: "eventServices.js", url: $("#COMMON_ROOT").attr("href") + "/services/eventServices.js"},
        {name: "vertxEventBus.js", url: $("#COMMON_ROOT").attr("href") + "/services/vertxEventBus.js"}
    ])
    .factory('resourceLoaderService', ['$rootScope', '$q', '$log', '$timeout',
        function ($rootScope, $q, $log, $timeout) {

            // Load JS remote resources, evalute them and insert into DOM, returns a promise
            function preloadJSResourcesReal($rootScope, $q, $log, $timeout, appResources, resourcePostProcessCB) {
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
                    var promise = preloadJSResourcesReal($rootScope, $q, $log, $timeout, appResources, resourcePostProcessCB);
                    return promise;
                }
            }
        }])
    .config(['$controllerProvider', '$compileProvider', '$filterProvider', '$provide', '$sceProvider', '$rootScopeProvider',
        '$locationProvider', '$injector', '$routeProvider', '$httpProvider', 'COMMON_DYNAMIC_RESOURCES', 'COMMON_ROOT',
        function ($controllerProvider, $compileProvider, $filterProvider, $provide, $sceProvider, $rootScopeProvider, $locationProvider, $injector, $routeProvider, $httpProvider, COMMON_DYNAMIC_RESOURCES, COMMON_ROOT) {
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

            vamiCommon.lazy = {
                controller: $controllerProvider.register,
                directive: $compileProvider.directive,
                filter: $filterProvider.register,
                factory: $provide.factory,
                service: $provide.service,
                injector: $injector,
                httpProvider: $httpProvider
            }
        }
    ]);

vamiCommon.run(['$q', '$rootScope', '$log', '$injector', 'resourceLoaderService', 'VERTX_SEND_EVENT', 'VERTX_RESPONSE_EVENT', 'COMMON_DYNAMIC_RESOURCES',
    function ($q, $rootScope, $log, $injector, resourceLoaderService, VERTX_SEND_EVENT, VERTX_RESPONSE_EVENT, COMMON_DYNAMIC_RESOURCES) {

        var html = '<div id="loading-spinner"></div>';
        $(document.body).append(html);

        resourceLoaderService.preloadJSResources(COMMON_DYNAMIC_RESOURCES).then(function () {

            var eventServices = $injector.get('eventServices');
            var LOADING = $("#loading-spinner");

            eventServices.registerForEvent(VERTX_SEND_EVENT, function () {
                $log.debug("VERTX_SEND_EVENT listener triggered");
                LOADING.show();
            });
            eventServices.registerForEvent(VERTX_RESPONSE_EVENT, function () {
                $log.debug("VERTX_RESPONSE_EVENT listener triggered");
                LOADING.hide();
            });

            vamiCommon.lazy.httpProvider.interceptors.push(['$rootScope', '$q', function ($rootScope, $q) {
                return {
                    request: function (config) {
                        LOADING.show();
                        return config || $q.when(config);
                    },
                    requestError: function (rejection) {
                        LOADING.hide();
                        return $q.reject(rejection);
                    },
                    response: function (config) {
                        LOADING.hide();
                        return config || $q.when(config);
                    },
                    responseError: function (rejection) {
                        LOADING.hide();
                        return $q.reject(rejection);
                    }
                };
            }]);
        });
    }]);
