(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgStatsChart', function () {

		return {
			restrict: 'E',
			template: '<lvg-chart type="Line" data="data" options="chartOptions" height="160" class="img-responsive"></lvg-chart>',
			scope: {
				chartData: "="
			},
			link: function (scope) {

				if (scope.chartData === undefined) {
					return;
				}

				scope.chartOptions = {
					bezierCurve: false,
					scaleIntegersOnly: true,
					showTooltips: false,
					scaleBeginAtZero: true,
					responsive: true,
					maintainAspectRatio: false
				};

				function getSortedIndexes(array) {
					var sortedIndexes = [];
					for (var index in array) {
						sortedIndexes.push(parseInt(index));
					}
					sortedIndexes.sort();
					return sortedIndexes;
				}

				scope.$watch('chartData', function () {
					var data = {
						labels: [],
						datasets: []
					};
					data.datasets.push({label: 'Created', strokeColor: '#c00', fillColor: 'rgba(204,0,0, 0.3)', pointColor: '#c00', data: []});
					data.datasets.push({label: 'Closed', strokeColor: '#3c3', fillColor: 'rgba(51,204,51, 0.3)', pointColor: '#3c3', data: []});

					var sortedCreatedClosedIndexes = getSortedIndexes(scope.chartData);
					for (var i = 0; i < sortedCreatedClosedIndexes.length; i++) {
						var index = sortedCreatedClosedIndexes[i];
						data.labels.push(new Date(index).toLocaleDateString());

						data.datasets[0].data.push(scope.chartData[index]['first']);
						data.datasets[1].data.push(scope.chartData[index]['second']);
					}
					scope.data = data;
				});


			}
		}
	});
})();
