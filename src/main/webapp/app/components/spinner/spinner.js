(function() {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgSpinner', {
		template : '<div class="bounce1"></div>'
				+ '<div class="bounce2"></div><div class="bounce3"></div>'
	});
})();