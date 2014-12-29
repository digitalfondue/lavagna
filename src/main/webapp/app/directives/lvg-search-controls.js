(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSearchControls', function () {
		return {
			restrict: 'E',
			templateUrl: 'partials/fragments/lvg-search-controls.html'
		}
	});
})();