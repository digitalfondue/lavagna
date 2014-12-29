(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardSidebarFragment', function () {
		return {
			templateUrl: 'partials/fragments/card-sidebar-fragment.html',
			restrict: 'E'
		}
	});
})();