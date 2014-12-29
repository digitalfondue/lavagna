(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgUserRo', function ($filter, $rootScope, UserCache) {

		var loadUser = function (userId, placeholder) {
			UserCache.user(userId).then(function (user) {
				placeholder.text($filter('formatUser')(user));
				if (user.enabled) {
					placeholder.removeClass('user-disabled');
				} else {
					placeholder.addClass('user-disabled');
				}
			});
		};

		return {
			restrict: 'A',
			transclude: true,
			scope: true,
			template: "<span class=\"lvg-user-placeholder fake-user-link\"></span><span data-ng-transclude></span>",
			link: function ($scope, element, attrs) {
				var unregister = $scope.$watch(attrs.lvgUserRo, function (userId) {
					if (userId == undefined) {
						return;
					}
					var placeholder = element.find('.lvg-user-placeholder');

					loadUser(userId, placeholder);

					var unbind = $rootScope.$on('refreshUserCache-' + userId, function () {
						loadUser(userId, placeholder);
					});
					$scope.$on('$destroy', unbind);

					unregister();
				});
			}
		};
	});

})();
