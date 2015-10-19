(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgComponentProjectManageLabels', ProjectManageLabelsComponent);

    function ProjectManageLabelsComponent($rootScope, $q, $filter, $translate, Notification, LabelCache, Label) {
        return {
            restrict: 'E',
            scope: true,
            bindToController: {
                project: '='
            },
            controller: ProjectManageLabelsController,
            controllerAs: 'manageLabelsCtrl',
            templateUrl: 'app/components/project/manage/labels/labels.html'
        }
    };

    function ProjectManageLabelsController($scope, $rootScope, $q, $filter, $translate, Notification, LabelCache, Label) {
        var ctrl = this;
        ctrl.view = {};

        var projectName = ctrl.project.shortName;

        var loadLabels = function () {
            LabelCache.findByProjectShortName(projectName).then(function (labels) {
                ctrl.labels = labels;
                ctrl.userLabels = {};
                ctrl.labelsListValues = {};
                ctrl.labelNameToId = {};
                for (var k in labels) {
                    ctrl.labelNameToId[labels[k].name] = k;
                    if (labels[k].domain === 'USER') {
                        ctrl.userLabels[k] = labels[k];
                    }
                }
            });
        };
        loadLabels();

        var unbind = $rootScope.$on('refreshLabelCache-' + projectName, loadLabels);
        $scope.$on('$destroy', unbind);

        ctrl.addNewLabel = function (labelToAdd) {
            labelToAdd = labelToAdd || {value: undefined, color: undefined, type: undefined};

            var labelColor = $filter('parseHexColor')(labelToAdd.color);

            if (ctrl.labelNameToId[labelToAdd.name] !== undefined) {
                if (labelToAdd.color !== null && labelToAdd.color !== ""
                    && labelToAdd.color !== undefined && labelToAdd.type !== undefined
                    && ctrl.userLabels[ctrl.labelNameToId[labelToAdd.name]].color != labelColor) {
                    Label.update(ctrl.labelNameToId[labelToAdd.name], {name: labelToAdd.name, color: labelColor, type: labelToAdd.type}).then(function () {
                        return ctrl.userLabels[ctrl.labelNameToId[labelToAdd.name]];
                    }).then(function() {
                        Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.success'}, false);
                    }, function(error) {
                        Notification.addAutoAckNotification('success', {key: 'notification.project-manage-labels.add.error'}, false);
                    });
                } else {
                    var defer = $q.defer();
                    defer.resolve(ctrl.userLabels[$scope.labelNameToId[labelToAdd.name]]);
                }
            } else {
                Label.add(projectName, {name: labelToAdd.name, color: labelColor, type: labelToAdd.type, unique: labelToAdd.unique}).then(function() {
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

        ctrl.view.labelOptions = getLabelOptions();
    };
})();
