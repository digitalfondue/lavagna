(function() {
    var components = angular.module('lavagna.components');

    components.component('lvgNavbarProject', {
        templateUrl: 'app/components/navbar/project/navbar-project.html',
        bindings: {
            project: '='
        },
        controller: function($window, User, $mdSidenav, $rootScope, $state) {
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

             var currentTabIndex = function(_state) {
                return tabs.indexOf(_state);
             }

             var tabs = ['project.boards','project.milestones','project.statistics'];

             ctrl.navigationState = currentTabIndex($state.current.name);
             $rootScope.$on('$stateChangeSuccess',
                function (event, toState, toParams, fromState, fromParams) {
                    ctrl.navigationState = currentTabIndex($state.current.name);
                }
             );
        }
    });
})();
