(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectManageAnonymousUsersAccessCtrl', function ($scope, $stateParams, User, Permission, project) {

		$scope.project = project;

		var load = function () {
			Permission.forProject($stateParams.projectName).findUsersWithRole('ANONYMOUS').then(function (res) {
				var userHasRole = false;
				for (var i = 0; i < res.length; i++) {
					if (res[i].provider === 'system' && res[i].username === 'anonymous') {
						userHasRole = true;
					}
				}
				$scope.userHasRole = userHasRole;
			});
		}

		load();
		
		$scope.$watch('userHasRole', function(newVal, oldVal) {
			if(newVal == undefined || oldVal == undefined || newVal == oldVal) {
				return;
			}
			User.byProviderAndUsername('system', 'anonymous').then(function (res) {
				if (newVal) {
					Permission.forProject($stateParams.projectName).addUserToRole(res.id, 'ANONYMOUS').then(load).catch(function(error) {
						Notification.addAutoAckNotification('error', {key: 'notification.project-manage-anon.enable.error'}, false);
					});
				} else {
					Permission.forProject($stateParams.projectName).removeUserToRole(res.id, 'ANONYMOUS').then(load).catch(function(error) {
						Notification.addAutoAckNotification('error', {key: 'notification.project-manage-anon.disable.error'}, false);
					});
				}
			})
		});
	});
})();