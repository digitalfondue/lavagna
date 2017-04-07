(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgSidebarContent', {
        templateUrl: 'app/components/sidebar-content/sidebar-content.html',
        controller: function ($window, $http, User, EventBus, StompClient) {
            var ctrl = this;

            var v;
            var currentlyFetching = {then: function (f) {
                f();
            }};

            var updateUser = function () {
                return User.currentCachedUser().then(function (user) {
                    ctrl.user = user;

                    return user;
                });
            };

            ctrl.$onInit = function () {
                updateUser().then(function (user) {
                    EventBus.on('refreshUserCache-' + user.id, updateUser);
                });

                v = setInterval(function () {
                    currentlyFetching = $http.get('api/keep-alive');
                }, 15 * 1000);
            };

            ctrl.logout = function () {
                clearInterval(v);
                currentlyFetching.then(function () {
                    User.current().then(function (u) {
                        StompClient.disconnect(function () {
                            $http.post('logout/' + u.provider + '/').then(function () {
                                $window.location.reload();
                            });
                        });
                    });
                });
            };
        }
    });
}());
