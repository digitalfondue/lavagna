(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardFragmentRo', function () {
		return {
			templateUrl: 'partials/fragments/board-card-menu-card-fragment.html',
			restrict: 'E'
		}
	});
})();