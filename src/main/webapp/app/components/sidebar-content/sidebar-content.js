(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgSidebarContent', {
        templateUrl: 'app/components/sidebar-content/sidebar-content.html',
        controller: function($window, $http, User, StompClient) {
        	var ctrl = this;

            var currentlyFetching = {then: function (f) {
                f();
            }};

            var v = setInterval(function () {
                currentlyFetching = $http.get('api/keep-alive');
            }, 15 * 1000);

            ctrl.logout = function () {
                clearInterval(v);
                currentlyFetching.then(function () {
                    User.current().then(function (u) {
                        StompClient.disconnect(function () {
                            $http.post('logout' + '/' + u.provider + '/').then(function (res) {
                            	$window.location.reload();
                            });
                        });
                    });
                });
            };

        }
    });
})();