(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgRebuildIfChange', ['$parse', function ($parse) {
        return {
            transclude: true,
            restrict: 'E',
            link: function ($scope, $element, $attr, ctrl, $transclude) {
                var expression = $parse($attr.toWatch);
                var currentHash;

                $scope.$watch(function () { return expression($scope); }, function (newValue) {
                    if (currentHash !== newValue) {
                        currentHash = newValue;
                        $transclude($scope, function (transEl) {
                            $element.empty();
                            $element.append(transEl);
                        });
                    }
                });
            }
        };
    }]);
}());
