(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgManageLabel', function ($rootScope, $modal, Label, $stateParams, LabelCache, Notification, $filter) {
		return {
			restrict: 'A',
			scope: {
				label: '=lvgManageLabel',
			},
			templateUrl: "partials/project/fragments/project-manage-label.html",
			controller: function ($scope) {
				var projectName = $stateParams.projectName;
				
				var emitRefreshEvent = function() {
					$scope.$emit('refreshLabelCache-' + projectName);
				}
				
				$scope.removeLabel = function () {
					Label.remove($scope.label.id).then(function() {
						Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.remove.success'}, false);
					}, function(error) {
						Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.remove.error'}, false);
					}).then(emitRefreshEvent);
				};
				
				$scope.updateLabel = function (values) {
					var labelColor = $filter('parseHexColor')(values.color);
					Label.update($scope.label.id, {name: values.name, color: labelColor, type: values.type}).then(function() {
						Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.update.success'}, false);
					}, function(error) {
						Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.update.error'}, false);
					}).then(emitRefreshEvent);
				};
				
				$scope.labelsListValues = {};
				var loadListValues = function () {
					if ($scope.label.type === 'LIST') {
						LabelCache.findLabelListValues($scope.label.id).then(function (listValues) {
							$scope.labelsListValues = listValues;
						});
					}
				};
				
				var isLabelInUse = function() {
					Label.useCount($scope.label.id).then(function(useCount) {
						$scope.useCount = useCount;
					});
				}
				
				var loadLabelData = function() {
					loadListValues();
					isLabelInUse();
				}
				
				loadLabelData();
				
				var unbind = $rootScope.$on('refreshLabelCache-' + projectName, loadLabelData);
				$scope.$on('$destroy', unbind);
				
				$scope.editLabelList = function (label, labelsListValues) {
					$modal.open({
						templateUrl: 'partials/project/fragments/project-modal-edit-label.html',
						windowClass: 'lavagna-modal',
						controller: function ($rootScope, $scope, LabelCache, Label) {
							$scope.l = label;
							$scope.labelsListValues = labelsListValues;
							
							$scope.swapLabelListValues = function (first, second) {
								Label.swapLabelListValues($scope.l.id, {first: first, second: second});
							};

							$scope.addLabelListValue = function (val) {
								Label.addLabelListValue($scope.l.id, {value: val});
							};

							$scope.removeLabelListValue = function (labelListValueId) {
								Label.removeLabelListValue(labelListValueId);
							};
							
							var loadListValues = function () {
								if ($scope.l.type === 'LIST') {
									LabelCache.findLabelListValues($scope.l.id).then(function (listValues) {
										$scope.labelsListValues = listValues;
									});
								}
							};
							
							var unbind = $rootScope.$on('refreshLabelCache-' + projectName, loadListValues);
							$scope.$on('$destroy', unbind);
						},
						size: 'lg'
					});
				};
				
			}
		};
	})
})();