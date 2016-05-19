(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageProject', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageController,
        templateUrl: 'app/components/project/manage/project/project.html'
    });

    function ProjectManageController($rootScope, Project, ProjectCache, Notification) {
        var ctrl = this;
        ctrl.view = {};

        var shortName = ctrl.project.shortName;

        var unbind = $rootScope.$on('refreshProjectCache-' + shortName, function () {
            ProjectCache.project(shortName).then(function (p) {
                ctrl.project = p;
            });
        });
        
        ctrl.$onDestroy = function() {
        	unbind();
        }
        

        ctrl.update = function (project) {
            Project.update(project).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-home.update.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-home.update.error'}, false);
            });
        };
    }
})();
