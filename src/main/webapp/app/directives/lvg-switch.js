(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSwitch', function () {
		
		return {
			restrict: 'E',
			scope: {
				model : '=control'
			},
			template: "<div class=\"{{mainClass}}\" ng-class=\"{\'active\': model}\" ng-click=\"toggle()\"><div class=\"button\"></div></div>",
			link: function ($scope, element, attrs) {
				
				$scope.mainClass = attrs.switchClass;
				
				$scope.toggle = function() {
					$scope.model = !$scope.model;
				}
				
			}
		};
	});
})();