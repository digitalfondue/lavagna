(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageMilestones', {
        bindings: {
            project: '='
        },
        controller: ProjectManageMilestonesController,
        controllerAs: 'manageMilestonesCtrl',
        templateUrl: 'app/components/project/manage/milestones/milestones.html'
    });

    function ProjectManageMilestonesController($rootScope, $scope, LabelCache, Label, Notification) {

        var ctrl = this;
        ctrl.view = {};

        var projectName = ctrl.project.shortName;

        ctrl.milestoneUseCount = {};

        var loadLabel = function () {
            LabelCache.findByProjectShortName(projectName).then(function (labels) {
                for (var k in labels) {
                    if (labels[k].name === 'MILESTONE' && labels[k].domain === 'SYSTEM') {
                        ctrl.milestoneLabel = labels[k];
                        LabelCache.findLabelListValues(labels[k].id).then(function (values) {
                            ctrl.milestoneValues = values;
                        });
                        break;
                    }
                }
            });
        };
        loadLabel();

        var unbind = $rootScope.$on('refreshLabelCache-' + projectName, loadLabel);
        $scope.$on('$destroy', unbind);

        ctrl.update = function(val) {
            Label.updateLabelListValue(val).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-milestones.update.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-milestones.update.error'}, false);
            });
        };

        ctrl.addLabelListValue = function (val) {
            Label.addLabelListValue(ctrl.milestoneLabel.id, {value: val});
        };

        ctrl.updateCount = function(id) {
            Label.countLabelListValueUse(id).then(function(cnt) {
                ctrl.milestoneUseCount[id] = cnt;
            });
        }

        ctrl.removeLabelListValue = function (labelListValueId) {
            Label.removeLabelListValue(labelListValueId).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-milestones.remove.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-milestones.remove.error'}, false);
            });
        };

        ctrl.moveLabelListValue = function (id, order) {
            Label.moveLabelListValue(ctrl.milestoneLabel.id, {first: id, second: order});
        };

        ctrl.swapLabelListValues = function (first, second) {
            Label.swapLabelListValues(ctrl.milestoneLabel.id, {first: first, second: second});
        };

        ctrl.closeMilestone = function(val) {
            Label.createLabelListValueMetadata(val.id, 'status', 'CLOSED');
        };

        ctrl.openMilestone = function(val) {
            Label.removeLabelListValueMetadata(val.id, 'status');
        };

    };
})();
