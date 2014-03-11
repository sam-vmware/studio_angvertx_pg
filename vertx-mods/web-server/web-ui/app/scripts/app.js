'use strict';
/**
 * Created by samueldoyle
 */

angular.element(document).ready(function () {
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

    angular.bootstrap(document.getElementById('vamiAppContainer'), ['vamiApp']);
    //    angular.bootstrap(document.getElementById('mainAppContainer'), ['mainApp']);
});
