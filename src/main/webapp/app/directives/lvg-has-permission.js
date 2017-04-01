(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgHasPermission', function (User, $stateParams) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.addClass('lavagna-hide');
                if (attrs.ownerId !== undefined) {
                    User.currentCachedUser().then(function (u) {
                        if (u.id === $scope.$eval(attrs.ownerId)) {
                            element.removeClass('lavagna-hide');
                        }
                    });
                }

                if ('withProject' in attrs) {
                    User.hasPermission(attrs.lvgHasPermission, attrs.withProject).then(function () {
                        element.removeClass('lavagna-hide');
                    });
                } else {
                    User.hasPermission(attrs.lvgHasPermission, $stateParams.projectName).then(function () {
                        element.removeClass('lavagna-hide');
                    });
                }
            }
        };
    });

    directives.directive('lvgHasNotPermission', function (User, $stateParams) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.addClass('lavagna-hide');
                User.hasPermission(attrs.lvgHasNotPermission, $stateParams.projectName).then(function () {
                }, function () {
                    element.removeClass('lavagna-hide');
                });
            }
        };
    });

    directives.directive('lvgHasAllPermissions', function (User, $stateParams) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.addClass('lavagna-hide');
                User.hasAllPermissions(attrs.lvgHasAllPermissions.split(','), $stateParams.projectName).then(function () {
                    element.removeClass('lavagna-hide');
                });
            }
        };
    });

    directives.directive('lvgHasAtLeastOnePermission', function (User, $stateParams) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.addClass('lavagna-hide');
                User.hasAtLeastOnePermission(attrs.lvgHasAtLeastOnePermission.split(','), $stateParams.projectName).then(function () {
                    element.removeClass('lavagna-hide');
                });
            }
        };
    });
}());
