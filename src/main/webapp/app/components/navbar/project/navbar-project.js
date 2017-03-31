(function () {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarProject', {
        templateUrl: 'app/components/navbar/project/navbar-project.html',
        bindings: {
            project: '<'
        },
        controller: function (User, $mdSidenav, $state, $window) {
            var ctrl = this;

            ctrl.$state = $state;

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
