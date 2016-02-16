(function() {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarProject', {
        templateUrl: 'app/components/navbar/project/navbar-project.html',
        bindings: {
            project: '='
        },
        controller: function($window, User, $mdSidenav, $state) {
             var ctrl = this;
             
             ctrl.$state = $state;

             User.currentCachedUser().then(function (u) {
                 ctrl.navbarUser = u;
             });

             ctrl.toggleSidebar = function() {
            	 $mdSidenav('left').toggle();
             }

             ctrl.login = function () {
                 var reqUrlWithoutContextPath = $window.location.pathname.substr($("base").attr('href').length - 1);
                 $window.location.href = 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
             }
        }
    });
})();
