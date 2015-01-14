(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgUser', function ($filter, $q, $rootScope, UserCache) {

		var providerMap = {
			'demo': 'fa-laptop',
			'ldap': 'fa-book',
			'persona': 'fa-user',
			'oauth.google': 'fa-google',
			'oauth.bitbucket': 'fa-bitbucket',
			'oauth.github': 'fa-github',
			'oauth.twitter': 'fa-twitter'
		};

		var generateTooltipHTML = function (user) {
			var userDisplayText = $filter('formatUser')(user);
			var userProviderClass = providerMap[user.provider];
			return '<div class=\"lavagna-user-tooltip\">' +
				'<div class=\"provider\"><i class=\"fa ' + userProviderClass + '\"></i></div>' +
				'<div class=\"name\">' + userDisplayText + '</div>' +
				'<div class=\"user-info\"><ul>' +
				'<li><i class=\"fa fa-user\"></i>' + user.username + '</li>' +
				(user.email != null ? '<li><i class=\"fa fa-envelope\"></i><a href="mailto:' + user.email + '">' + user.email + '</a></li>' : '') +
				'</ul></div>' +
				'</div>';
		};

		var loadUser = function (userId, placeholder, providerPlaceholder, linkPlaceholder) {
			var deferred = $q.defer();
			UserCache.user(userId).then(function (user) {
				placeholder.text($filter('formatUser')(user));
				if (user.enabled) {
					placeholder.removeClass('user-disabled');
				} else {
					placeholder.addClass('user-disabled');
				}
				deferred.resolve(generateTooltipHTML(user));
				linkPlaceholder.attr('href', "#/user/" + user.provider + "/" + user.username);
			});
			return deferred.promise;
		};

		return {
			restrict: 'A',
			transclude: true,
			scope: true,
			template: '<span data-bindonce="readOnly">'
				+ '<span data-bo-if="!readOnly">'
				+	'<a data-lvg-user-tooltip data-lvg-user-tooltip-html=\"{{tooltipHTML}}\" class=\"lvg-user-link-placeholder\"><span class=\"lvg-user-placeholder\"></span><span data-ng-transclude></span></a>'
				+ '</span><span data-bo-if="readOnly">'
				+	'<span class=\"lvg-user-placeholder\"></span><span data-ng-transclude></span>'
				+ '</span></span>',
			link: function ($scope, element, attrs) {
				$scope.readOnly = attrs.readOnly != undefined;
				
				var unregister = $scope.$watch(attrs.lvgUser, function (userId) {
					if (userId == undefined) {
						return;
					}
					var placeholder = element.find('.lvg-user-placeholder');
					var providerPlaceholder = element.find('.lvg-user-provider-placeholder');
					var linkPlaceholder = element.find('.lvg-user-link-placeholder');

					loadUser(userId, placeholder, providerPlaceholder, linkPlaceholder).then(function (html) {
						$scope.tooltipHTML = html;
					});

					var unbind = $rootScope.$on('refreshUserCache-' + userId, function () {
						loadUser(userId, placeholder, providerPlaceholder, linkPlaceholder).then(function (html) {
							$scope.tooltipHTML = html;
						});
					});
					$scope.$on('$destroy', unbind);

					unregister();
				});
			}
		};
	});

	directives.directive('lvgUserTooltip', function () {
		return {
			restrict: 'A',
			link: function ($scope, element, attrs) {
				attrs.$observe('lvgUserTooltipHtml', function (html) {
					if (html === undefined || html === null || html.length === 0) {
						return;
					}
					$(element).tooltip({
						items: '[data-lvg-user-tooltip], [lvg-user-tooltip]',
						content: html,
						show: { delay: 500 },
						close: function (event, ui) {
							ui.tooltip.hover(
								function () {
									$(this).stop(true).fadeTo(400, 1);
								},
								function () {
									$(this).fadeOut("400", function () {
										$(this).remove();
									})
								}
							);
						}
					});
				});
			}
		}
	});
})();
