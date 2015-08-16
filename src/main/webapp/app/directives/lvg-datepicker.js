(function () {

    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgDatepicker', function () {
        return {
            restrict: 'A',
            require: '?ngModel',
            link: function (scope, element, attrs, ngModel) {
                element.datepicker({
                    firstDay: moment().startOf('week').isoWeekday(),
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
