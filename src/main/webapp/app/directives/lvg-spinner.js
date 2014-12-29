(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSpinner', function () {
		return {
			restrict: 'E',
			template: '<div class="spinner"><div class="bounce1"></div><div class="bounce2"></div><div class="bounce3"></div></div>',
			link: function ($scope) {
			}
		}
	})
})();