(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgNotification', function (Notification) {
		return {
			templateUrl: 'partials/fragments/notifications-fragment.html'
		}
	})
})();