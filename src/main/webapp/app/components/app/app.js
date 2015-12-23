(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.controller('LvgAppController', AppController);

    function AppController($window, User) {
        var ctrl = this;

        var titleStack = [];
        var baseTitle = "Lavagna";

        ctrl.title = baseTitle;

        ctrl.setTitle = function(title) {
            titleStack.push(title);
            ctrl.title = title;
        }

        ctrl.revertTitle = function() {
            titleStack.pop();
            ctrl.title = titleStack.length > 0 ? titleStack[titleStack.length - 1]: baseTitle;
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
