(function() {

	'use strict';

	var directives = angular.module('lavagna.directives');
	
	directives.directive('lvgDownload', function(CONTEXT_PATH) {
		return function($scope, element, attrs) {
			$(element).click(function() {
				var url = CONTEXT_PATH+$(this).attr('href');
				$(element).after("<iframe src='" + url + "' class='ng-cloak'></iframe>");
				return false;
			});
		}
	})
})();