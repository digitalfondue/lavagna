(function() {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarAbout', {
        templateUrl: 'app/components/navbar/about/navbar-about.html',
        bindings: {},
        controller: function($window, User, Sidebar, $rootScope, $state) {
             var ctrl = this;

             User.currentCachedUser().then(function (u) {
                 ctrl.navbarUser = u;
             });

             ctrl.toggleSidebar = function() {
                Sidebar.toggle();
             }

             ctrl.login = function () {
                 var reqUrlWithoutContextPath = $window.location.pathname.substr($("base").attr('href').length - 1);
                 $window.location.href = 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
             }

             ctrl.navigationState = $state.current.name;
             $rootScope.$on('$stateChangeSuccess',
                function (event, toState, toParams, fromState, fromParams) {
                    ctrl.navigationState = $state.current.name;
                }
             );
        }
    });
})();
