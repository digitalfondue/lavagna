(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgStatsPanel', function (Project, $filter) {
		return {
			templateUrl: 'partials/fragments/stats-panel.html',
			restrict: 'E',
			scope: {
				item: '=',
				stats: '='
			},
			link: function ($scope) {
				$scope.chartOptions = {animation : false};

				var createNormalizedData = function (stats) {
					var normalizedStats = [];
					normalizedStats.push({value: stats.openTaskCount, color: $filter('color')(stats.openTaskColor).color});
					normalizedStats.push({value: stats.closedTaskCount, color: $filter('color')(stats.closedTaskColor).color});
					normalizedStats.push({value: stats.backlogTaskCount, color: $filter('color')(stats.backlogTaskColor).color});
					normalizedStats.push({value: stats.deferredTaskCount, color: $filter('color')(stats.deferredTaskColor).color});
					return normalizedStats;
				};

				var hasTasks = function (stats) {
					var totalTasks = parseInt(stats.openTaskCount) +
						parseInt(stats.closedTaskCount) +
						parseInt(stats.backlogTaskCount) +
						parseInt(stats.deferredTaskCount);
					return totalTasks > 0;
				};

				$scope.$watch('stats', function (stats) {
					if (stats === undefined) {
						return;
					}
					$scope.statistics = stats;
					if (hasTasks(stats)) {
						$scope.chartData = createNormalizedData(stats);
					}
				});
			}
		}
	});

	directives.directive('lvgBoardPanel', function (Board) {
		return {
			templateUrl: 'partials/fragments/stats-panel-board.html',
			restrict: 'E',
			scope: {
				board: '=',
				projectShortName: '='
			},
			link: function ($scope) {
				$scope.stats = undefined;
				Board.taskStatistics($scope.board.shortName).then(function (stats) {
					$scope.stats = stats;
				});
			}
		}
	});

	directives.directive('lvgProjectPanel', function (Project) {
		return {
			templateUrl: 'partials/fragments/stats-panel-project.html',
			restrict: 'E',
			scope: {
				project: '='
			},
			link: function ($scope) {
				$scope.stats = undefined;
				Project.taskStatistics($scope.project.shortName).then(function (stats) {
					$scope.stats = stats;
				});
			}
		}
	});
})();