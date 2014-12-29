(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgOnEsc', function () {
		return {
			restrict: 'A',
			scope: {
				onEsc: '=',
				onEscEval: '@'
			},
			link: function ($scope, element) {
				$(element).keyup(function (e) {
					if (e.keyCode == 27) {
						if ($scope.onEsc || $scope.onEscEval) {
							$scope.$apply(function () {
								if ($scope.onEsc) {
									$scope.onEsc = false;
								}
								if ($scope.onEscEval) {
									$scope.$parent.$eval($scope.onEscEval);
								}
							});
						}
						return false;
					}
				});
			}
		}
	});
})();