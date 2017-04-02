(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageLabels', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageLabelsController,
        templateUrl: 'app/components/project/manage/labels/labels.html'
    });

    function ProjectManageLabelsController(EventBus, $q, $filter, $translate, $mdDialog, Notification, LabelCache, Label) {
        var ctrl = this;

        var projectName = ctrl.project.shortName;

        var loadLabels = function () {
            LabelCache.findByProjectShortName(projectName).then(function (labels) {
                ctrl.labels = labels;
                ctrl.userLabels = {};
                ctrl.labelsListValues = {};
                ctrl.labelNameToId = {};

                angular.forEach(labels, function (label, labelId) {
                    ctrl.labelNameToId[label.name] = labelId;
                    if (label.domain === 'USER') {
                        ctrl.userLabels[labelId] = label;
                    }
                });
            });
        };

        loadLabels();

        var unbind = EventBus.on('refreshLabelCache-' + projectName, loadLabels);

        ctrl.$onDestroy = function () {
            unbind();
        };

        ctrl.showAddLabelDialog = function () {
            $mdDialog.show({
                templateUrl: 'app/components/project/manage/labels/add-label-dialog.html',
                controller: function (labelOptions) {
                    var ctrl = this;

                    ctrl.labelOptions = labelOptions;

                    ctrl.add = function (labelToAdd) {
                        $mdDialog.hide(labelToAdd);
                    };

                    ctrl.close = function () {
                        $mdDialog.cancel();
                    };
                },
                controllerAs: 'ctrl',
                resolve: {
                    labelOptions: getLabelOptions
                }
            }).then(function (labelToAdd) {
                return addNewLabel(labelToAdd);
            });
        };

        function getLabelOptions() {
            return [
                { name: $filter('translate')('project.manage.labels.types.NULL'), type: 'NULL' },
                { name: $filter('translate')('project.manage.labels.types.STRING'), type: 'STRING' },
                { name: $filter('translate')('project.manage.labels.types.TIMESTAMP'), type: 'TIMESTAMP' },
                { name: $filter('translate')('project.manage.labels.types.INT'), type: 'INT' },
                { name: $filter('translate')('project.manage.labels.types.CARD'), type: 'CARD' },
                { name: $filter('translate')('project.manage.labels.types.USER'), type: 'USER' },
                { name: $filter('translate')('project.manage.labels.types.LIST'), type: 'LIST' }
            ];
        }

        function addNewLabel(labelToAdd) {
            labelToAdd = labelToAdd || {value: undefined, color: undefined, type: undefined};

            var labelColor = $filter('parseHexColor')(labelToAdd.color);

            if (ctrl.labelNameToId[labelToAdd.name] !== undefined) {
                if (labelToAdd.color !== null && labelToAdd.color !== ''
                    && labelToAdd.color !== undefined && labelToAdd.type !== undefined
                    && ctrl.userLabels[ctrl.labelNameToId[labelToAdd.name]].color !== labelColor) {
                    Label.update(ctrl.labelNameToId[labelToAdd.name], {name: labelToAdd.name, color: labelColor, type: labelToAdd.type}).then(function () {
                        return ctrl.userLabels[ctrl.labelNameToId[labelToAdd.name]];
                    }).then(function () {
                        Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.success'}, false);
                    }, function () {
                        Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.error'}, false);
                    });
                } else {
                    var defer = $q.defer();

                    defer.resolve(ctrl.userLabels[ctrl.labelNameToId[labelToAdd.name]]);
                }
            } else {
                Label.add(projectName, {name: labelToAdd.name, color: labelColor, type: labelToAdd.type, unique: labelToAdd.unique}).then(function () {
                    Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.success'}, false);
                }, function () {
                    Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.error'}, false);
                });
            }
        }
    }
}());
