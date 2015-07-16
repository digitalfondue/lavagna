(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgToFocus', function ($timeout) {
		return function (scope, elem, attrs) {
			scope.$watch(attrs.lvgToFocus, function (newval) {
				if (newval) {
					$timeout(function () {
						elem.focus();
					}, 0, false);
				}
			});
		};
	});
})();