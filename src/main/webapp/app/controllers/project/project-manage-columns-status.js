(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectManageColumnsStatusCtrl', function ($stateParams, $scope, $filter, Project, project) {

		$scope.project = project;

		var loadColumnsDefinition = function () {
			Project.columnsDefinition($stateParams.projectName).then(function (definitions) {
				$scope.columnsDefinition = definitions;
				$scope.columnDefinition = {}; //data-ng-model
				for (var d = 0; d < definitions.length; d++) {
					var definition = definitions[d];
					$scope.columnDefinition[definition.id] = { color: $filter('parseIntColor')(definition.color) };
				}
			});
		};
		loadColumnsDefinition();

		$scope.isNotUniqueColor = function (color) {
			for (var definitionId in $scope.columnsDefinition) {
				if ($scope.convertIntToColorCode($scope.columnsDefinition[definitionId].color) === color) {
					return true;
				}
			}
			return false;
		};

		$scope.convertIntToColorCode = function (intColor) {
			return $filter('parseIntColor')(intColor)
		}

		$scope.updateColumnDefinition = function (definition, color) {
			Project.updateColumnDefinition($stateParams.projectName, definition, $filter('parseHexColor')(color)).then(function() {
				Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.success'}, false);
			} , function(error) {
				Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.error'}, false);
			}).then(loadColumnsDefinition);
		};

	});
})();