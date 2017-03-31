(function () {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarBoard', {
        templateUrl: 'app/components/navbar/board/navbar-board.html',
        bindings: {
            board: '<',
            project: '<'
        },
        controller: function (User, $mdSidenav, $state, $window) {
            var ctrl = this;

            User.currentCachedUser().then(function (u) {
                ctrl.navbarUser = u;
            });

            ctrl.toggleSidebar = function () {
                $mdSidenav('left').toggle();
            };

            ctrl.login = function () {
                $window.location.href = User.loginUrl();
            };
        }
    });
}());
