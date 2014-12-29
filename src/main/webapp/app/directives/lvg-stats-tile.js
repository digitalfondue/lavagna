(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgStatsTile', function () {

		return {
			restrict: 'E',
			templateUrl: 'partials/fragments/stats-tile.html',
			scope: {
				tileTitle: "=",
				value: "=",
				subtitle: "=?",
				valueColor: "=?"
			},
			link: function ($scope) {
				if ($scope.valueColor === undefined) {
					$scope.valueColor = 0;
				}
			}
		}
	});
})();
