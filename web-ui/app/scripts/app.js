'use strict';

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

    angular.module("template/tabs/tab.html", []).run(["$templateCache", function ($templateCache) {
        $templateCache.put("template/tabs/tab.html",
                '<li ng-class="{active: active, disabled: disabled}">' +
                '    <a ng-click="select()" tab-heading-transclude>{{heading}}</a>' +
                '</li>');
    }]);


    angular.bootstrap(document.getElementById('vamiAppContainer'), ['vamiApp']);
//    angular.bootstrap(document.getElementById('mainAppContainer'), ['mainApp']);
});
