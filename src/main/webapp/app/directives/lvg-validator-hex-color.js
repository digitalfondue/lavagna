(function () {
    'use strict';

    angular.module('lavagna.directives').directive('lvgValidatorHexColor', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attr, ctrl) {
                var pattern = /^#(?:[0-9a-f]{3}){1,2}$/i;

                function hexColorValidator(ngModelValue) {
                    ctrl.$setValidity('color', pattern.test(ngModelValue));

                    return ngModelValue;
                }
                ctrl.$parsers.push(hexColorValidator);
            }
        };
    });
}());
