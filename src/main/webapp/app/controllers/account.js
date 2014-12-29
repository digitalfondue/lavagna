(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AccountCtrl', function ($scope, User, Notification) {
		
		
		User.currentCachedUser().then(function(user) {
			$scope.userNameProfile = user.username;
			$scope.userProvider = user.provider;
			$scope.userUsername = user.username;
		});
		

		$scope.profile = {};
		
		$scope.isCurrentUser = true;

		var loadUser = function (u) {
			$scope.user = u;
			$scope.profile.email = u.email;
			$scope.profile.displayName = u.displayName;
			$scope.profile.emailNotification = u.emailNotification;

		};

		$scope.clearAllTokens = function () {
			User.clearAllTokens().then(function() {
				Notification.addAutoAckNotification('success', {key: 'notification.user.tokenCleared.success'}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', {key: 'notification.user.tokenCleared.error'}, false);
			});
		};

		User.current().then(loadUser);

		$scope.update = function (profile) {
			User.updateProfile(profile)
				.then(User.invalidateCachedUser)
				.then(User.current).then(loadUser).then(function() {
					Notification.addAutoAckNotification('success', {key: 'notification.user.update.success'}, false);
				}, function(error) {
					Notification.addAutoAckNotification('error', {key: 'notification.user.update.error'}, false);
				});
		}
	});
})();