(function () {

    'use strict';

    var module = angular.module('lavagna.controllers');

    module.controller('ProjectManageMilestonesCtrl', function ($rootScope, $stateParams, $scope, $modal, $translate, Notification, LabelCache, Label, project) {

        $scope.project = project;

        $scope.milestoneUseCount = {};

        var loadLabel = function () {
            LabelCache.findByProjectShortName($stateParams.projectName).then(function (labels) {
                for (var k in labels) {
                    if (labels[k].name === 'MILESTONE' && labels[k].domain === 'SYSTEM') {
                        $scope.milestoneLabel = labels[k];
                        LabelCache.findLabelListValues(labels[k].id).then(function (values) {
                            $scope.milestoneValues = values;
                        });
                        break;
                    }
                }
            });
        };
        loadLabel();

        var unbind = $rootScope.$on('refreshLabelCache-' + project.shortName, loadLabel);
        $scope.$on('$destroy', unbind);

        $scope.update = function (val) {
            Label.updateLabelListValue(val).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-milestones.update.success'}, false);
            }, function (error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-milestones.update.error'}, false);
            });
        };

        $scope.addLabelListValue = function (val) {
            Label.addLabelListValue($scope.milestoneLabel.id, {value: val}).then(function () {
                $scope.newMilestoneValue = null;
            });
        };

        $scope.updateCount = function (id) {
            Label.countLabelListValueUse(id).then(function (cnt) {
                $scope.milestoneUseCount[id] = cnt;
            });
        };

        $scope.removeLabelListValue = function (labelListValueId) {
            Label.removeLabelListValue(labelListValueId).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-milestones.remove.success'}, false);
            }, function (error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-milestones.remove.error'}, false);
            });
        };

        $scope.moveLabelListValue = function (id, order) {
            Label.moveLabelListValue($scope.milestoneLabel.id, {first: id, second: order});
        };

        $scope.swapLabelListValues = function (first, second) {
            Label.swapLabelListValues($scope.milestoneLabel.id, {first: first, second: second});
        };

        $scope.closeMilestone = function (val) {
            Label.updateLabelListValueMetadata(val.id, 'status', 'CLOSED');
        };

        $scope.openMilestone = function (val) {
            Label.removeLabelListValueMetadata(val.id, 'status');
        };

        $scope.updateReleaseDate = function (val) {
            if (val.metadata.releaseDate) {
                Label.updateLabelListValueMetadata(val.id, 'releaseDate', val.metadata.releaseDate);
            } else {
                Label.removeLabelListValueMetadata(val.id, 'releaseDate');
            }
        };

    });
})();
