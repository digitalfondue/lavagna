(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectMilestonesCtrl', function ($stateParams, $scope, Card, User, Label, Notification, StompClient, project) {

		$scope.sidebarOpen = true;
		$scope.project = project;

		$scope.milestoneOpenStatus = {};

		$scope.showArray = function (array, minLength) {
			if (!array) {
				return false;
			}
			return Object.keys(array).length > minLength;
		};

		var orderByStatus = function (milestone) {
			var insertStatusIfExists = function (milestone, source, target, status) {
				if (source[status] != undefined) {
					target[target.length] = {status: status, count: source[status]};
					milestone.totalCards += source[status];
				}
			};

			milestone.totalCards = 0;
			var sorted = [];
			insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "BACKLOG");
			insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "OPEN");
			insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "DEFERRED");
			insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "CLOSED");
			$scope.cardsCountByStatus[milestone.labelListValue.value] = sorted;
		};

		$scope.moveDetailToPage = function (milestone, page) {
			User.hasPermission('READ', $stateParams.projectName).then(function () {
				return Card.findCardsByMilestoneDetail($stateParams.projectName, milestone.labelListValue.value, page);
			}).then(function (response) {
				milestone.detail = response;
				milestone.currentPage = response.cards.currentPage + 1;
			});
		};

		var loadMilestonesInProject = function () {
			User.hasPermission('READ', $stateParams.projectName).then(function () {
				return Card.findCardsByMilestone($stateParams.projectName);
			}).then(function (response) {
				$scope.cardsByMilestone = response.milestones;
				$scope.cardsCountByStatus = [];
				for (var index in response.milestones) {
					var milestone = response.milestones[index];
					orderByStatus(milestone);
					if ($scope.milestoneOpenStatus[milestone.labelListValue.value]) {
						$scope.moveDetailToPage(milestone, 0);
					}
				}
				$scope.statusColors = response.statusColors;
			});
		};

		loadMilestonesInProject();

		StompClient.subscribe($scope, '/event/project/' + $stateParams.projectName + '/label-value', loadMilestonesInProject);

		StompClient.subscribe($scope, '/event/project/' + $stateParams.projectName + '/label', loadMilestonesInProject);

		$scope.clearMilestoneDetail = function (milestone) {
			milestone.detail = null;
			milestone.currentPage = 1;
		};

		$scope.loadMilestoneDetail = function (milestone) {
			$scope.moveDetailToPage(milestone, 0);
		};

		$scope.toggleMilestoneOpenStatus = function (milestone) {
			var currentOpenStatus = $scope.milestoneOpenStatus[milestone.labelListValue.value];
			currentOpenStatus ? $scope.clearMilestoneDetail(milestone) : $scope.loadMilestoneDetail(milestone);
			$scope.milestoneOpenStatus[milestone.labelListValue.value] = !currentOpenStatus;
		};

		$scope.updateMilestone = function (milestone, newName) {
			var newLabelValue = jQuery.extend({}, milestone.labelListValue);
			newLabelValue.value = newName;
			Label.updateLabelListValue(newLabelValue);
		};
	});
})();