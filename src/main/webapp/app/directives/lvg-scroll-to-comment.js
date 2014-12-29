(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgScrollToComment', function ($window, $timeout) {
		return {
			scope: {
				commentId: "=lvgScrollToComment"
			},
			link: function (scope, element) {
				if ($window.location.hash === ('#' + scope.commentId)) {
					$timeout(function () {
						$(element).addClass('lavagna-highlight').get(0).scrollIntoView();
					});
				}
			}
		};
	})
})();
