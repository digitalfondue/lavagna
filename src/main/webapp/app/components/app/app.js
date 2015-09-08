(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.controller('LvgAppController', AppController);

    function AppController($window, User) {
        var ctrl = this;

        ctrl.title = "Lavagna";

        ctrl.setTitle = function(title) {
            ctrl.title = title;
        }

        User.current().then(function (u) {
            ctrl.currentUser = u;
        });

        ctrl.login = function () {
            var reqUrlWithoutContextPath = $window.location.pathname.substr($("base").attr('href').length - 1);
            $window.location.href = 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
        };
    }
})();
