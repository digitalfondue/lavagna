(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('HomeCtrl', function($scope, Project, User, Notification, StompClient) {

		$scope.projectPage = 1;
		$scope.projectsPerPage = 10;
		$scope.maxVisibleProjectPages = 3;

		var loadProjects = function() {
			Project.list().then(function(projects) {
				$scope.projects = projects;
				$scope.totalProjects = projects.length;

				$scope.switchProjectPage($scope.projectPage);
			});
		};

		loadProjects();

		$scope.switchProjectPage = function(page) {
			$scope.currentProjects = $scope.projects.slice((page - 1) * $scope.projectsPerPage,
					((page - 1) * $scope.projectsPerPage) + $scope.projectsPerPage);
		};

		$scope.cardPage = 1;
		$scope.maxVisibleCardPages = 3;

		var loadUserCards = function(page) {
			User.isAuthenticated().then(function() {return User.hasPermission('SEARCH')}).then(function() {
				User.cards(page).then(function(cards) {
					$scope.userCards = cards.cards;
					$scope.totalOpenCards = cards.totalCards;
					$scope.cardsPerPage = cards.itemsPerPage;
				});
			});
		};

		loadUserCards($scope.cardPage - 1);

		$scope.fetchUserCardsPage = function(page) {
			loadUserCards(page - 1);
		};

		StompClient.subscribe($scope, '/event/project', loadProjects);

		$scope.suggestProjectShortName = function(project) {
			if(project == null ||project.name == null || project.name == "") {
				return;
			}
			Project.suggestShortName(project.name).then(function(res) {
				project.shortName = res.suggestion;
			});
		};
		
		$scope.$watch('project.shortName', function(newVal) {
			if(newVal !== undefined && newVal !== null) {
				Project.checkShortName(newVal).then(function(res) {
					$scope.checkedShortName = res;
				});
			} else {
				$scope.checkedShortName = undefined;
			}
		});


		$scope.createProject = function(project) {
			project.shortName = project.shortName.toUpperCase();
			Project.create(project).then(function() {
				$scope.project = {};
				$scope.checkedShortName = undefined;
				Notification.addAutoAckNotification('success', {key: 'notification.project.creation.success'}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', {key: 'notification.project.creation.error'}, false);
			});
		};
	});
})();