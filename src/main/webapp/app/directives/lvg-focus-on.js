(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgFocusOn', ['$parse', '$timeout', function ($parse, $timeout) {
        return {
            restrict: 'A',
            link: function ($scope, $element, attrs) {
                var expression = $parse(attrs.lvgFocusOn);

                $scope.$watch(function () { return expression($scope); }, function (newValue) {
                    if (newValue === true) {
                        $timeout(function () { $element.focus(); }, 0, false);
                    }
                });
            }
        };
    }]);
}());
