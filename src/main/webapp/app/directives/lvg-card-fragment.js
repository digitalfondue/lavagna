(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardFragment', function () {
		return {
			templateUrl: 'partials/fragments/card-fragment.html',
			restrict: 'E'
		}
	});
})();