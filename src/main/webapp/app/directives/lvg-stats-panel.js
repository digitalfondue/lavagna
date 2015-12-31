(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

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