(function () {

    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgDatepicker', function (LOCALE_FIRST_DAY_OF_WEEK) {
        return {
            restrict: 'A',
            require: '?ngModel',
            link: function (scope, element, attrs, ngModel) {
                element.datepicker({
                    firstDay: LOCALE_FIRST_DAY_OF_WEEK - 1, //1 is sunday, 2 monday...
                    dateFormat: 'dd.mm.yy',
                    onSelect: function (date) {
                        scope.$apply(function () {
                            ngModel.$setViewValue(date);
                        });
                    }
                });
            }
        };
    });
})();
