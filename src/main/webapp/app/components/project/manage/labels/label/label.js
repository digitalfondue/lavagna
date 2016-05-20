(function() {
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

    function ProjectManageLabelController($scope, $rootScope, $filter, $mdDialog, Notification, LabelCache, Label) {
        var ctrl = this;
        ctrl.view = {};

        var projectName = ctrl.project.shortName;

        var emitRefreshEvent = function() {
            $scope.$emit('refreshLabelCache-' + projectName);
        }

        ctrl.removeLabel = function () {
            Label.remove(ctrl.label.id).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.remove.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.remove.error'}, false);
            }).then(emitRefreshEvent);
        };

        ctrl.updateLabel = function (values) {
            var labelColor = $filter('parseHexColor')(values.color);
            Label.update(ctrl.label.id, {name: values.name, color: labelColor, type: values.type}).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.update.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.update.error'}, false);
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

        var isLabelInUse = function() {
            Label.useCount(ctrl.label.id).then(function(useCount) {
                ctrl.view.useCount = useCount;
            });
        }

        var loadLabelData = function() {
            loadListValues();
            isLabelInUse();
        }

        loadLabelData();

        var unbind = $rootScope.$on('refreshLabelCache-' + projectName, loadLabelData);
        ctrl.$onDestroy = function() {
        	unbind();
        }

        ctrl.editLabelList = function () {
        	$mdDialog.show({
        		templateUrl: 'app/components/project/manage/labels/label/edit-label-values.html',
        		controller: function ($rootScope, $scope, LabelCache, Label, label, labelListValues) {
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

                    ctrl.updateCount = function(id) {
                        Label.countLabelListValueUse(id).then(function(cnt) {
                            ctrl.labelListValueUseCount[id] = cnt;
                        });
                    };
                    
                    ctrl.closeDialog = function() {
                    	$mdDialog.hide();
                    };

                    //handle refresh event
                    var loadListValues = function () {
                        if (ctrl.label.type === 'LIST') {
                            LabelCache.findLabelListValues(ctrl.label.id).then(function (listValues) {
                                ctrl.labelListValues = listValues;
                            });
                        }
                    };

                    var unbind = $rootScope.$on('refreshLabelCache-' + projectName, loadListValues);
                    $scope.$on('$destroy', unbind);
                },
                controllerAs: 'manageLabelValuesCtrl',
                resolve: {
                    label: function() {
                        return ctrl.label;
                    },
                    labelListValues: function() {
                        return ctrl.labelListValues;
                    }
                },
                fullscreen: true
        	});
        };

    };
})();
