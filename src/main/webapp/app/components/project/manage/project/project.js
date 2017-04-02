(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageProject', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageController,
        templateUrl: 'app/components/project/manage/project/project.html'
    });

    function ProjectManageController($filter, EventBus, Project, ProjectCache, Notification) {
        var ctrl = this;
        var shortName = ctrl.project.shortName;

        function loadColumnsDefinition() {
            Project.columnsDefinition(shortName).then(function (definitions) {
                ctrl.columnsDefinition = definitions;
                ctrl.columnDefinition = {}; // data-ng-model
                for (var d = 0; d < definitions.length; d++) {
                    var definition = definitions[d];

                    ctrl.columnDefinition[definition.id] = { color: $filter('parseIntColor')(definition.color) };
                }
            });
        }

        var unbind = EventBus.on('refreshProjectCache-' + shortName, function () {
            ProjectCache.project(shortName).then(function (p) {
                ctrl.project = p;
            });
        });

        ctrl.$onInit = function () {
            loadColumnsDefinition();
        };

        ctrl.$onDestroy = function () {
            unbind();
        };

        ctrl.update = function (project) {
            Project.update(project).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-home.update.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-home.update.error'}, false);
            });
        };

        ctrl.updateColumnDefinition = function (definition, color) {
            Project.updateColumnDefinition(shortName, definition, $filter('parseHexColor')(color)).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-columns-status.update.error'}, false);
            }).then(loadColumnsDefinition);
        };
    }
}());
