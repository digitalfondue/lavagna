(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgMatch', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                modelValueToMatch: '=lvgMatch'
            },
            link: function ($scope, $element, $attrs, ngModel) {
                ngModel.$validators.lvgMatch = function (ngModelValue) {
                    return ngModelValue === $scope.modelValueToMatch;
                };

                $scope.$watch($scope.modelValueToMatch, function () {
                    ngModel.$validate();
                });
            }
        };
    });
}());
