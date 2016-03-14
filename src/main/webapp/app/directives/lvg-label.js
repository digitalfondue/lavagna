(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgLabel', function () {
		return {
			transclude: true,
			restrict: 'E',
			scope: {
				value: '<',
				name: '<'
			},
			template: function($element, $attrs) {
				var isReadOnly = $attrs.readOnly != undefined;
				return '<span>'
					+'<span ng-bind="::name"></span>'
					+'<span ng-if="::(type !== \'NULL\')">: </span>'
					+'<lvg-label-val '+(isReadOnly ? ' data-read-only ' : '')+' value="value"></lvg-label-val>'
					+'<span data-ng-transclude></span>'
					+'</span>';
			},
			link: function ($scope, $element, $attrs) {
				if ($scope.value === null || $scope.value === undefined || $scope.name === null || $scope.name === undefined) {
					return;
				}
				$scope.type = $scope.value.labelValueType || $scope.value.type;
			}
		};
	})
})();