(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgChart', {
        template: '<canvas></canvas>',
        bindings: {
            data: '<',
            options: '<',
            type: '@',
            width: '@',
            height: '@'
        },
        controller: ['$element', ChartController]
    });

    function ChartController($element) {
        var ctrl = this;

        var chartInstance;

        ctrl.$onChanges = function onChanges(changes) {
            if (changes.data || changes.options) {
                var value = ctrl.data;

                if (value === undefined) {
                    return;
                }

                var baseWidth = 150;
                var baseHeight = 150;

                var canvas = $element.find('canvas')[0];
                var context = canvas.getContext('2d');

                canvas.width = ctrl.width || baseWidth;
                canvas.height = ctrl.height || baseHeight;
                context.canvas.style.maxHeight = canvas.height + 'px';

                if (chartInstance !== undefined) {
                    chartInstance.destroy();
                    chartInstance = undefined;
                }

                var chart = new Chart(context);
                var chartType = ctrl.type || 'Line';

                chartInstance = chart[chartType](ctrl.data, ctrl.options);
            }
        };

        ctrl.$onDestroy = function onDestroy() {
            if (chartInstance) {
                chartInstance.destroy();
                chartInstance = undefined;
            }
        };
    }
}());
