'use strict';

var mainApp = angular.module('mainApp', [
    'ui.bootstrap',
    'ui.router'
]);

mainApp
    .constant('MAIN_ROOT', $("#MAIN_ROOT").attr("href"))
    .config(['$provide', '$stateProvider', '$urlRouterProvider', 'MAIN_ROOT',
        function ($provide, $stateProvider, $urlRouterProvider, MAIN_ROOT) {
            var home = {
                name: 'home',
                url: '/',
                templateUrl: MAIN_ROOT + '/views/mainApp.html'
            };
            $stateProvider.state(home);
        }])
    .run(['$state', function ($state) {
        $state.transitionTo('home');
    }]);
