(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSwitch', function () {

		return {
			restrict: 'E',
			scope: {
				model: '=control',
				change: '='
			},
			template: "<div class=\"{{mainClass}}\" ng-class=\"{\'active\': model}\" ng-click=\"handleChange()\"><div class=\"button\"></div></div>",
			link: function ($scope, element, attrs) {
				$scope.mainClass = attrs.switchClass;

				$scope.handleChange = function() {
				    $scope.model = !$scope.model;
				    if($scope.change != undefined && $scope.change != null) {
				        $scope.change($scope.model);
				    }
				}
			}
		};
	});
})();
