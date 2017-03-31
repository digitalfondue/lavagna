(function () {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarSearch', {
        templateUrl: 'app/components/navbar/search/navbar-search.html',
        bindings: {
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
