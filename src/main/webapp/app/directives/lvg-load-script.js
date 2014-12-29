(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	/** ngSrc inside a script in a partial don't seems to work. */
	directives.directive('lvgLoadScript', function () {
		return {
			restrict: 'A',
			scope: {
				lvgLoadScript: "@"
			},
			link: function ($scope, element, attrs) {
				element.addClass('ng-hide');
				$.getScript($scope.lvgLoadScript).done(function (script, textStatus) {
					// here in theory we could enable the button
				})
					.fail(function (jqxhr, settings, exception) {
						// here in theory we could disable the button + error
						// message
						element.removeClass('ng-hide');
					});
			}
		}
	});

})();