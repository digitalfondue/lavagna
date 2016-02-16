(function() {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarSearch', {
        templateUrl: 'app/components/navbar/search/navbar-search.html',
        bindings: {
            project: '='
        },
        controller: function($window, User, $mdSidenav) {
             var ctrl = this;

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
