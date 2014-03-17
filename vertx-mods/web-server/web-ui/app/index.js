'use strict';
angular.element(document).ready(function () {
    var mainApp = angular.module('mainApp', [
        'http-auth-interceptor'
    ])
        .controller('loginController', ['$scope', '$http', 'authService', function loginController($scope, $http, authService) {
            $scope.formData = {};
            $scope.login = function () {
                $http({
                    method: 'POST',
                    url: '/auth/login',
                    data: $.param($scope.formData),
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                }).success(function (data, status) {
                    if (data.result == 'ok') {
                        authService.loginConfirmed();
                    } else {
                        var errorData = data.errorData;
                        $scope.message = errorData.error.message;
                        alert($scope.message);
                    }
                });
            };
        }])
        .directive("mainApplication", ['$window', function ($window) {
            return {
                restrict: "C",
                link: function (scope, elem, attrs) {
                    elem.removeClass("loading-spinner");

                    /*  var login = elem.find("#login-container");
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
                     });*/
                    scope.$on("event:auth-loginConfirmed", function () {
                        $window.location.href = "/modules/main/views/mainApp.html"
                    });
                }
            }
        }]);

    angular.bootstrap(document.getElementById('main-app-container'), ['mainApp']);
});

