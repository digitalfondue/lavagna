(function() {

    'use strict';

    var components = angular.module('lavagna.components');
    components.directive('lvgComponentProjectManageStatus', ProjectManageStatusComponent);

    function ProjectManageStatusComponent(Project, Notification, $filter) {
        return {
            restrict: 'E',
            scope: true,
            bindToController: {
                project: '='
            },
            controller: ProjectManageStatusController,
            controllerAs: 'manageStatusCtrl',
            templateUrl: 'app/components/project/manage/status/status.html'
        }
    };

    function ProjectManageStatusController(Project, Notification, $filter) {

        var ctrl = this;
        var projectName = ctrl.project.shortName;

        var loadColumnsDefinition = function () {
            Project.columnsDefinition(projectName).then(function (definitions) {
                ctrl.columnsDefinition = definitions;
                ctrl.columnDefinition = {}; //data-ng-model
                for (var d = 0; d < definitions.length; d++) {
                    var definition = definitions[d];
                    ctrl.columnDefinition[definition.id] = { color: $filter('parseIntColor')(definition.color) };
                }
            });
        };
        loadColumnsDefinition();

        ctrl.isNotUniqueColor = function (color) {
            for (var definitionId in ctrl.columnsDefinition) {
                if (ctrl.convertIntToColorCode(ctrl.columnsDefinition[definitionId].color) === color) {
                    return true;
                }
            }
            return false;
        };

        ctrl.convertIntToColorCode = function (intColor) {
            return $filter('parseIntColor')(intColor)
        }

        ctrl.updateColumnDefinition = function (definition, color) {
            Project.updateColumnDefinition(projectName, definition, $filter('parseHexColor')(color)).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.success'}, false);
            } , function(error) {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.error'}, false);
            }).then(loadColumnsDefinition);
        };

    };
})();
