(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.controller('LvgAppController', AppController);

    function AppController($window, $rootScope, User, Title) {
        var ctrl = this;

        $rootScope.$on('titlechange', function(event, title) {
            console.log('title change: %s', title);
            //first load, location is happening before resolves
            if(Title.isCacheEmpty()) {
                ctrl.title = title;
            }
        });

        // put current title in the cache
        $rootScope.$on('$locationChangeStart', function(e, n, o) {
            console.log('location start change, new=%s, old=%s', n, o);
            // not first load
            if(ctrl.title !== null && ctrl.title !== undefined) {
                var title = ctrl.title.slice(0);
                Title.put(o, title);
            }
        });

        // get the current title from the cache
        $rootScope.$on('$locationChangeSuccess', function(e, n, o) {
            console.log('location success change, new=%s, old=%s', n, o);
            // not first load
            if(ctrl.title !== null && ctrl.title !== undefined) {
                var currentTitle = Title.get();
                var oldLocationTitle = Title.getCachedURL(o);
                var newLocationTitle = Title.getCachedURL(n);
                if(currentTitle == oldLocationTitle) { //back button scenario
                    ctrl.title = newLocationTitle;
                } else {
                    ctrl.title = currentTitle;
                }
            }
        });

        User.currentCachedUser().then(function (u) {
            ctrl.currentUser = u;
        });

        ctrl.login = function () {
            var reqUrlWithoutContextPath = $window.location.pathname.substr($("base").attr('href').length - 1);
            $window.location.href = 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
        };
    }
})();
