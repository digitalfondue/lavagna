(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectManageHomeCtrl', function ($scope, $rootScope, ProjectCache, Project, project) {

			$scope.project = project;
			$scope.item = project;

			var unbind = $rootScope.$on('refreshProjectCache-' + project.shortName, function () {
				ProjectCache.project(project.shortName).then(function (p) {
					$scope.project = p;
					$scope.item = p;
				});
			});
			$scope.$on('$destroy', unbind);

			$scope.update = function (project) {
				Project.update(project).then(function() {
					Notification.addAutoAckNotification('success', {key: 'notification.project-manage-home.update.success'}, false);
				}, function(error) {
					Notification.addAutoAckNotification('error', {key: 'notification.project-manage-home.update.error'}, false);
				});
			};
		}
	);
})();