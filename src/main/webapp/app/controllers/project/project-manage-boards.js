(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectManageBoardsCtrl', function ($scope, $rootScope, Project, Board, project) {
			$scope.project = project;
			
			$scope.boards = {};
			$scope.boardsStatus = {};
			
			var reloadBoards = function () {
				Project.findBoardsInProject(project.shortName).then(function (boards) {
					$scope.boards = boards;
				});
			};

			reloadBoards();
			
			$scope.update = function (boardToUpdate) {
				Board.update(boardToUpdate).then(function (b) {
					reloadBoards();
				}).then(function() {
					Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.update.success'}, false);
				}, function(error) {
					Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.update.error'}, false);
				});
			};
			
			$scope.archive = function (ab) {
				var boardToUpdate = {shortName: ab.shortName, name: ab.name, description: ab.description, archived: true};
				Board.update(boardToUpdate).then(function (b) {
					reloadBoards();
				}).then(function() {
					Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.archive.success'}, false);
				}, function(error) {
					Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.archive.error'}, false);
				});
			};
			
			$scope.unarchive = function (ab) {
				var boardToUpdate = {shortName: ab.shortName, name: ab.name, description: ab.description, archived: false};
				Board.update(boardToUpdate).then(function (b) {
					reloadBoards();
				}).then(function() {
					Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.unarchive.success'}, false);
				}, function(error) {
					Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.unarchive.error'}, false);
				});
			};
		}
	);
})();