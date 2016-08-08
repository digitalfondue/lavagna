(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageStatus', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageStatusController,
        templateUrl: 'app/components/project/manage/status/status.html'
    });

    function ProjectManageStatusController(Project, Notification, $filter) {

        var ctrl = this;
        
        ctrl.updateColumnDefinition = updateColumnDefinition
        
        ctrl.$onInit = function() {
        	loadColumnsDefinition();
        }
        
        function loadColumnsDefinition() {
            Project.columnsDefinition(ctrl.project.shortName).then(function (definitions) {
                ctrl.columnsDefinition = definitions;
                ctrl.columnDefinition = {}; //data-ng-model
                for (var d = 0; d < definitions.length; d++) {
                    var definition = definitions[d];
                    ctrl.columnDefinition[definition.id] = { color: $filter('parseIntColor')(definition.color) };
                }
            });
        };

        function updateColumnDefinition(definition, color) {
            Project.updateColumnDefinition(ctrl.project.shortName, definition, $filter('parseHexColor')(color)).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.success'}, false);
            } , function(error) {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.error'}, false);
            }).then(loadColumnsDefinition);
        };

    };
})();
