(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageMilestones', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageMilestonesController,
        templateUrl: 'app/components/project/manage/milestones/milestones.html'
    });

    function ProjectManageMilestonesController(EventBus, $mdDialog, $translate, LabelCache, Label, Notification) {
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

        var unbind = EventBus.on('refreshLabelCache-' + projectName, loadLabel);

        ctrl.$onDestroy = function () {
            unbind();
        };

        ctrl.update = function (val) {
            Label.updateLabelListValue(val).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-milestones.update.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-milestones.update.error'}, false);
            });
        };

        ctrl.updateCount = function (id) {
            Label.countLabelListValueUse(id).then(function (cnt) {
                ctrl.milestoneUseCount[id] = cnt;
            });
        };

        ctrl.deleteMilestone = function (milestone, ev) {
            var confirm = $mdDialog.confirm()
                  .title($translate.instant('project.manage.milestone.dialog.delete.title'))
                  .textContent($translate.instant('project.manage.milestone.dialog.delete.message', {name: milestone.value}))
                  .targetEvent(ev)
                  .ok($translate.instant('button.yes'))
                  .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                return Label.removeLabelListValue(milestone.id);
            }).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-milestones.remove.success', parameters: {name: milestone.value}}, false);
            }, function (error) {
                if (error) {
                    Notification.addAutoAckNotification('error', {key: 'notification.project-manage-milestones.remove.error', parameters: {name: milestone.value}}, false);
                }
            });
        };

        ctrl.moveLabelListValue = function (id, order) {
            Label.moveLabelListValue(ctrl.milestoneLabel.id, {first: id, second: order});
        };

        ctrl.swapLabelListValues = function (first, second) {
            Label.swapLabelListValues(ctrl.milestoneLabel.id, {first: first, second: second});
        };

        ctrl.closeMilestone = function (val) {
            Label.updateLabelListValueMetadata(val.id, 'status', 'CLOSED');
        };

        ctrl.openMilestone = function (val) {
            Label.removeLabelListValueMetadata(val.id, 'status');
        };

        ctrl.updateReleaseDate = function (milestoneId, newDate) {
            Label.updateLabelListValueMetadata(milestoneId, 'releaseDate', newDate);
        };

        ctrl.removeReleaseDate = function (milestoneId) {
            Label.removeLabelListValueMetadata(milestoneId, 'releaseDate');
        };

        ctrl.showAddMilestoneDialog = function showAddMilestoneDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/project/manage/milestones/add-milestone-dialog.html',
                controller: function () {
                    var ctrl = this;

                    ctrl.view = {};
                    ctrl.addLabelListValue = addLabelListValue;
                    ctrl.close = function () {
                        $mdDialog.hide();
                    };
                },
                controllerAs: 'addMilestoneDialogCtrl'
            });
        };

        function addLabelListValue(val) {
            Label.addLabelListValue(ctrl.milestoneLabel.id, {value: val});
        }
    }
}());
