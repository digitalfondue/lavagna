(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.controller('LvgAppController', AppController);

    function AppController($window, User, Title) {
        var ctrl = this;

        Title.set('index.lavagna'); // basic title when the app loads

        User.currentCachedUser().then(function (u) {
            ctrl.currentUser = u;
        });

        ctrl.login = function () {
            var reqUrlWithoutContextPath = $window.location.pathname.substr($("base").attr('href').length - 1);
            $window.location.href = 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
        };
    }
})();
