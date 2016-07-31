(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSizeByVisibleColumns', function () {
		return {
			restrict: 'A',
			link: function ($scope, element, attrs) {
				$scope.$watch('boardCtrl.columns.length', function (length) {
					if(length === undefined) {
						return;
					}
					//290 = width of column + 4px left and right margin
					var width = length * (290 + 4 * 2);
                    element.css('width', width + "px");
				});
			}
		};
	})
})();