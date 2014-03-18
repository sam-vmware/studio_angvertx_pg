'use strict';
/**
 * Created by samueldoyle
 */
angular.element(document).ready(function () {
    vamiApp.lazy.controller('serviceTabsController', ['$q', '$log', '$scope', '$http', '$route', '$routeParams',
        '$compile', 'serviceTabsService', 'VAMI_ROOT',
        function ($q, $log, $scope, $http, $route, $routeParams, $compile, serviceTabsService, VAMI_ROOT) {
            /*        $route.current.templateUrl = 'partials/' + $routeParams.name + ".html";
             $http.get($route.current.templateUrl).then(function (msg) {
             $('#views').html($compile(msg.data)($scope));
             });*/

            $scope.tabs = serviceTabsService.getTabs();

            $scope.getActive = function () {
                return $scope.tabs.filter(function (tab) {
                    return tab.active;
                })[0];
            };

            $scope.switchTab = function () {
                var state = $state.current;
                var active = $scope.active();
                if (active.state != state.name) {
                    $log.debug("Transitioning to: " + state.name);
                    $state.go(state.name, null, {notify: false, location: true});
                }
            };

            $scope.activeByState = function (tab) {
                var stateURL = _.last($state.current.url.split('/'));
                if (tab) {
                    return (tab.name == stateURL);
                }
                return stateURL;
            };

            $scope.navType = 'pills';

        }]);
});

