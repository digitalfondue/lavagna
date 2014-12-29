(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgLabel', function () {
		return {
			transclude: true,
			restrict: 'AE',
			scope: {
				value: '=',
				name: '='
			},
			template: '<span data-bindonce="name">'
				+ '<span data-bo-bind="name"></span>'
				+ '<span data-bindonce="type"><span data-bo-if="type !== \'NULL\'">: </span></span>'
				+ '<span data-bindonce="readOnly"><span data-bo-if="readOnly"> <lvg-label-val data-read-only value="value"></lvg-label-val></span>'
				+ '<span data-bo-if="!readOnly"> <lvg-label-val value="value"></lvg-label-val></span></span>'
				+ '<span data-ng-transclude></span>'
				+ '</span>',
			link: function ($scope, $element, $attrs) {
				if ($scope.value === null || $scope.value === undefined || $scope.name === null || $scope.name === undefined) {
					return;
				}
				$scope.readOnly = $attrs.readOnly != undefined;
				$scope.type = $scope.value.labelValueType || $scope.value.type;
			}
		};
	})
})();