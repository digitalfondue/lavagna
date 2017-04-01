(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgOnEsc', ['$parse', function ($parse) {
        return {
            restrict: 'A',
            link: function ($scope, $element, attrs) {
                var expression = $parse(attrs.lvgOnEsc);

                $element.on('keydown', function (event) {
                    if (event.keyCode === 27) {
                        event.preventDefault();
                        event.stopPropagation();
                        $scope.$applyAsync(function () {
                            expression($scope);
                        });
                    }
                });
            }
        };
    }]);
}());
