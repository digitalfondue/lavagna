(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgStatsChart', {
        template: '<lvg-chart type="Line" data="$ctrl.data" options="$ctrl.chartOptions" height="160"></lvg-chart>',
        bindings: {
            chartData: '<'
        },
        controller: [StatsChartController]
    }
    );

    function StatsChartController() {
        var ctrl = this;

        ctrl.$onInit = function init() {
            ctrl.chartOptions = {
                bezierCurve: false,
                scaleIntegersOnly: true,
                showTooltips: false,
                scaleBeginAtZero: true,
                responsive: true,
                maintainAspectRatio: false,
                animation: false
            };
        };

        ctrl.$onChanges = function onChanges(changes) {
            if (!(changes.chartData && changes.chartData.currentValue)) {
                return;
            }
            var data = {
                labels: [],
                datasets: []
            };

            data.datasets.push({label: 'Created', strokeColor: '#c00', fillColor: 'rgba(204,0,0, 0.3)', pointColor: '#c00', data: []});
            data.datasets.push({label: 'Closed', strokeColor: '#3c3', fillColor: 'rgba(51,204,51, 0.3)', pointColor: '#3c3', data: []});

            var sortedCreatedClosedIndexes = getSortedIndexes(ctrl.chartData);

            for (var i = 0; i < sortedCreatedClosedIndexes.length; i++) {
                var index = sortedCreatedClosedIndexes[i];

                data.labels.push(new Date(index).toLocaleDateString());

                data.datasets[0].data.push(ctrl.chartData[index]['first']);
                data.datasets[1].data.push(ctrl.chartData[index]['second']);
            }
            ctrl.data = data;
        };

        function getSortedIndexes(array) {
            var sortedIndexes = [];

            for (var index in array) {
                if (array.hasOwnProperty(index)) {
                    sortedIndexes.push(parseInt(index));
                }
            }
            sortedIndexes.sort();

            return sortedIndexes;
        }
    }
}());
