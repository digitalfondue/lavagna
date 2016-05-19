(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageAccess', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageAccessController,
        controllerAs: 'manageAccessCtrl',
        templateUrl: 'app/components/project/manage/access/access.html'
    });

    function ProjectManageAccessController(User, Notification, Permission) {

        var ctrl = this;
        ctrl.view = {};

        var projectName = ctrl.project.shortName;

        var load = function () {
            Permission.forProject(projectName).findUsersWithRole('ANONYMOUS').then(function (res) {
                var userHasRole = false;
                for (var i = 0; i < res.length; i++) {
                    if (res[i].provider === 'system' && res[i].username === 'anonymous') {
                        userHasRole = true;
                    }
                }
                ctrl.userHasRole = userHasRole;
            });
        }

        load();

        ctrl.update = function(newValue) {
            updateAnonymousAccess(newValue);
        }

        var updateAnonymousAccess = function(newVal) {
            User.byProviderAndUsername('system', 'anonymous').then(function (res) {
                if (newVal) {
                    Permission.forProject(projectName).addUserToRole(res.id, 'ANONYMOUS').then(load).catch(function(error) {
                        Notification.addAutoAckNotification('error', {key: 'notification.project-manage-anon.enable.error'}, false);
                    });
                } else {
                    Permission.forProject(projectName).removeUserToRole(res.id, 'ANONYMOUS').then(load).catch(function(error) {
                        Notification.addAutoAckNotification('error', {key: 'notification.project-manage-anon.disable.error'}, false);
                    });
                }
            })
        }
    };

})();
