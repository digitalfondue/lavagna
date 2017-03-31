(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgIsAuthenticated', function (User) {
        return {
            restrict: 'A',
            link: function ($scope, element) {
                element.addClass('ng-hide');
                User.isAuthenticated().then(function () {
                    element.removeClass('ng-hide');
                });
            }
        };
    });

    directives.directive('lvgIsNotAuthenticated', function (User) {
        return {
            restrict: 'A',
            link: function ($scope, element) {
                element.addClass('ng-hide');
                User.isNotAuthenticated().then(function () {
                    element.removeClass('ng-hide');
                });
            }
        };
    });
}());
