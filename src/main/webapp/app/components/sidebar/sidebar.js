(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgSidebar', {
        templateUrl: 'app/components/sidebar/sidebar.html',
        controllerAs: 'lvgSidebar',
        controller: function($scope, $rootScope, $window, $http, User, StompClient, Sidebar) {
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
                                if (u.provider === 'persona' && res.data && res.data.redirectToSelf) {
                                    $window.location.href = $("base").attr('href') + 'logout/persona';
                                } else {
                                    $window.location.reload();
                                }
                            });
                        });
                    });
                });
            };

            var escapeHandler = function (e) {
                if (e.keyCode == 27) {
                    Sidebar.toggle();
                }
            };

            var clickHandler = function (event) {
                $scope.$apply(function() {
                    Sidebar.toggle();
                });
            };

            var openSidebar = function() {
                $("body").append($('<div id="sidebarBackdrop" class="lvg-modal-overlay lvg-modal-overlay-fade"></div>'));
                $("body").addClass('lvg-modal-open');
                $("#sidebarBackdrop").addClass('in');
                $(".lvg-left-sidebar").addClass("lvg-left-sidebar-open");
                $(document).bind('keyup', escapeHandler);
                $('#sidebarBackdrop').bind('click', clickHandler);
            };

            var closeSidebar = function() {
                $('#sidebarBackdrop').unbind('click', clickHandler);
                $(".lvg-left-sidebar").removeClass("lvg-left-sidebar-open");
                $('#sidebarBackdrop').removeClass('in');
                $('#sidebarBackdrop').remove();
                $("body").removeClass('lvg-modal-open');
                $(document).unbind('keyup', escapeHandler);
            }

            $rootScope.$on('$stateChangeSuccess',
                function (event, toState, toParams, fromState, fromParams) {
                    Sidebar.close();
                }
            );

            Sidebar.onOpen(openSidebar);
            Sidebar.onClose(closeSidebar);

            $scope.$on('$destroy', function () {
                clearInterval(v);
                Sidebar.unbindOpen(openSidebar);
                Sidebar.unbindClose(closeSidebar);
            });
        }
    })
})();
