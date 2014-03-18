'use strict';
/**
 * Created by samueldoyle
 * Simple Cache
 */
vamiCommon.lazy.factory('cacheService', ['$resource', '$cacheFactory', function ($resource, $cacheFactory) {
    var cache = $cacheFactory('commonService');
    var cacheSuffix = "_cacheService";
    return {
        getResource: function (id) {
            id += cacheSuffix;
            return cache.get(id);
        },
        setResource: function (id, value) {
            id += cacheSuffix;
            cache.put(id, value);
        },
        clearResource: function (id) {
            id += cacheSuffix;
            cache.remove(id);
        },
        clearAllResources: function () {
            cache.removeAll();
        }
    }
}]);

