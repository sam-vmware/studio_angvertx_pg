'use strict';
/**
 * Created by samueldoyle
 * ATM just add the spinner to the body
 */
vamiCommon.lazy.directive("commonBodyDirective", ['$document', function ($document) {
    return function (scope, element, attr) {
        var html = '<div id="loading-spinner"></div>';
        $document.append(html);
    };
}]);

