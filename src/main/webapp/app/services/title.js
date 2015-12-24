(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	services.factory('Title', function ($translate, $rootScope) {
        var currentTitle = null;
        var urlCache = {};

        return {
            set: function(_title, options) {
                $translate(_title, options).then(function(result) {
                    currentTitle = result;
                    $rootScope.$emit('titlechange', result);
                });
            },
            get: function() {
                return currentTitle;
            },

            put: function(url, title) {
                urlCache[url] = title;
            },

            getCachedURL: function(url) {
                return urlCache[url];
            },

            isCacheEmpty: function() {
                console.log($.isEmptyObject(urlCache));
                return $.isEmptyObject(urlCache);
            }
        }
	});
})();
