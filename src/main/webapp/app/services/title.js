(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	services.factory('Title', function ($translate, $rootScope, $document) {
        var currentTitle = null;

        var canSetTitle = false;
        var forceEmit = true;

        var emit = function() {
            var documentTitle = angular.element(window.document)[0].title;
            if(currentTitle === documentTitle && !forceEmit) {
                forceEmit = true;
                canSetTitle = true;
            } else {
                angular.element(window.document)[0].title = currentTitle;
            }
        };

        // put current title in the cache
        $rootScope.$on('$locationChangeStart', function(e, n, o) {
            console.log('location start change, new=%s, old=%s', n, o);
            if(n === o) { // first load yo
                canSetTitle = true;
            } else {
                forceEmit = false; // not the first load anymore, stop force emit
            }
        });

        // get the current title from the cache
        $rootScope.$on('$locationChangeSuccess', function(e, n, o) {
            console.log('location success change, new=%s, old=%s', n, o);
            if(n !== o) { // emit title when the url is changed, TODO: flag for back
                canSetTitle = false;
                emit();
            }
        });

        $rootScope.$on('$stateChangeStart', function(e, from, fromp, to, top) {
            console.log('state start change, from=%s to=%s', JSON.stringify(from), JSON.stringify(to));
            if(to.name !== "") {
                forceEmit = false; // first state change after first load
            }
        });

        $rootScope.$on('$stateChangeSuccess', function(e, from, fromp, to, top) {
            console.log('state success change, from=%s to=%s', JSON.stringify(from), JSON.stringify(to));
            if(to.name !== "") {
                canSetTitle = true;
            }
        });

        return {
            set: function(_title, options) {
                if(canSetTitle) {
                    $translate(_title, options).then(function(result) {
                        //prevent set the same title
                        if(currentTitle !== result) {
                            currentTitle = result;
                        }
                        if(forceEmit) {
                            emit();
                        }
                    });
                }
            },
            forceSet: function(_title, options) {
                forceEmit = true;
                this.set(_title, options);
            },
            get: function() {
                return currentTitle;
            }
        }
	});
})();
