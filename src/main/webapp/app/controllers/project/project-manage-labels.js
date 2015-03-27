(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectManageLabelsCtrl', function ($rootScope, $q, $stateParams, $scope, $filter, $translate, $modal, Notification, LabelCache, Label, project) {

			$scope.project = project;

			var loadLabel = function () {
				LabelCache.findByProjectShortName($stateParams.projectName).then(function (labels) {
					$scope.labels = labels;
					$scope.userLabels = {};
					$scope.labelsListValues = {};
					$scope.labelNameToId = {};
					for (var k in labels) {
						$scope.labelNameToId[labels[k].name] = k;
						if (labels[k].domain === 'USER') {
							$scope.userLabels[k] = labels[k];
						}
					}
				});
			};
			loadLabel();
			
			$scope.removeLabel = function (label) {
				Label.remove(label.id).then(loadLabel);
			};

			var unbind = $rootScope.$on('refreshLabelCache-' + project.shortName, loadLabel);
			$scope.$on('$destroy', unbind);

			$scope.addNewLabel = function (labelToAdd) {
				labelToAdd = labelToAdd || {value: undefined, color: undefined, type: undefined};

				var labelColor = $filter('parseHexColor')(labelToAdd.color);

				if ($scope.labelNameToId[labelToAdd.name] !== undefined) {
					if (labelToAdd.color !== null && labelToAdd.color !== "" && labelToAdd.color !== undefined && labelToAdd.type !== undefined && $scope.userLabels[$scope.labelNameToId[labelToAdd.name]].color != labelColor) {
						Label.update($scope.labelNameToId[labelToAdd.name], {name: labelToAdd.name, color: labelColor, type: labelToAdd.type}).then(function () {
							return $scope.userLabels[$scope.labelNameToId[labelToAdd.name]];
						}).then(function() {
							Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.success'}, false);
						}, function(error) {
							Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.error'}, false);
						});
					} else {
						var defer = $q.defer();
						defer.resolve($scope.userLabels[$scope.labelNameToId[labelToAdd.name]]);
					}
				} else {
					Label.add($stateParams.projectName, {name: labelToAdd.name, color: labelColor, type: labelToAdd.type, unique: labelToAdd.unique}).then(function() {
						Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.success'}, false);
					}, function(error) {
						Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.error'}, false);
					});
				}
			};

			var getLabelOptions = function () {
				return [
					{ name: $filter('translate')('partials.project.manage-labels.types.NULL'), type: "NULL" },
					{ name: $filter('translate')('partials.project.manage-labels.types.STRING'), type: "STRING" },
					{ name: $filter('translate')('partials.project.manage-labels.types.TIMESTAMP'), type: "TIMESTAMP" },
					{ name: $filter('translate')('partials.project.manage-labels.types.INT'), type: "INT" },
					{ name: $filter('translate')('partials.project.manage-labels.types.CARD'), type: "CARD" },
					{ name: $filter('translate')('partials.project.manage-labels.types.USER'), type: "USER" },
					{ name: $filter('translate')('partials.project.manage-labels.types.LIST'), type: "LIST" }
				];
			};

			$scope.labelOptions = getLabelOptions();
		}
	)
	;
})();