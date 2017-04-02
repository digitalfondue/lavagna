(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageLabelsLabel', {
        bindings: {
            project: '<',
            label: '<'
        },
        controller: ProjectManageLabelController,
        templateUrl: 'app/components/project/manage/labels/label/label.html'
    });

    function ProjectManageLabelController($scope, EventBus, $filter, $mdDialog, $translate, Notification, LabelCache, Label) {
        var ctrl = this;

        var projectName = ctrl.project.shortName;

        var emitRefreshEvent = function () {
            EventBus.emit('refreshLabelCache-' + projectName);
        };

        ctrl.deleteLabel = function (ev) {
            var confirm = $mdDialog.confirm()
                  .title($translate.instant('project.manage.labels.dialog.delete.title'))
                  .textContent($translate.instant('project.manage.labels.dialog.delete.message', {name: ctrl.label.name}))
                  .targetEvent(ev)
                  .ok($translate.instant('button.yes'))
                  .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                return Label.remove(ctrl.label.id);
            }).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.remove.success'}, false);
            }, function (error) {
                if (error) {
                    Notification.addAutoAckNotification('error', {key: 'notification.project-manage-labels.remove.error'}, false);
                }
            }).then(emitRefreshEvent);
        };

        ctrl.labelListValues = {};
        var loadListValues = function () {
            if (ctrl.label.type === 'LIST') {
                LabelCache.findLabelListValues(ctrl.label.id).then(function (listValues) {
                    ctrl.labelListValues = listValues;
                });
            }
        };

        var isLabelInUse = function () {
            Label.useCount(ctrl.label.id).then(function (useCount) {
                ctrl.useCount = useCount;
            });
        };

        var loadLabelData = function () {
            loadListValues();
            isLabelInUse();
        };

        loadLabelData();

        var unbind = EventBus.on('refreshLabelCache-' + projectName, loadLabelData);

        ctrl.$onDestroy = function () {
            unbind();
        };

        var updateLabel = function (values) {
            var labelColor = $filter('parseHexColor')(values.color);

            Label.update(ctrl.label.id, {name: values.name, color: labelColor, type: ctrl.label.type}).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.update.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.update.error'}, false);
            }).then(emitRefreshEvent);
        };

        ctrl.editLabel = function () {
            $mdDialog.show({
                templateUrl: 'app/components/project/manage/labels/label/edit-label.html',
                controller: function (EventBus, $scope, LabelCache, Label, label, labelListValues, projectName) {
                    var ctrl = this;

                    ctrl.label = label;
                    ctrl.labelListValues = labelListValues;

                    ctrl.labelListValueUseCount = {};

                    ctrl.swapLabelListValues = function (first, second) {
                        Label.swapLabelListValues(ctrl.label.id, {first: first, second: second});
                    };

                    ctrl.addLabelListValue = function (val) {
                        Label.addLabelListValue(ctrl.label.id, {value: val});
                    };

                    ctrl.removeLabelListValue = function (labelListValueId) {
                        Label.removeLabelListValue(labelListValueId);
                    };

                    ctrl.updateCount = function (id) {
                        Label.countLabelListValueUse(id).then(function (cnt) {
                            ctrl.labelListValueUseCount[id] = cnt;
                        });
                    };

                    ctrl.save = function (values) {
                        $mdDialog.hide(values);
                    };

                    ctrl.closeDialog = function () {
                        $mdDialog.cancel();
                    };

                    // handle refresh event
                    var loadListValues = function () {
                        if (ctrl.label.type === 'LIST') {
                            LabelCache.findLabelListValues(ctrl.label.id).then(function (listValues) {
                                ctrl.labelListValues = listValues;
                            });
                        }
                    };

                    var unbind = EventBus.on('refreshLabelCache-' + projectName, loadListValues);

                    $scope.$on('$destroy', unbind);
                },
                controllerAs: 'ctrl',
                resolve: {
                    label: function () {
                        return ctrl.label;
                    },
                    labelListValues: function () {
                        return ctrl.labelListValues;
                    },
                    projectName: function () {
                        return projectName;
                    }
                },
                fullscreen: true
            }).then(function (values) {
                return updateLabel(values);
            });
        };
    }
}());
