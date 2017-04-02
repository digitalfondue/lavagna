(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageAccess', {
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/manage/access/access.html',
        controller: ['User', 'Notification', 'Permission', ProjectManageAccessController],
    });

    function ProjectManageAccessController(User, Notification, Permission) {
        var ctrl = this;
        //

        ctrl.update = update;
        //

        ctrl.$onInit = load;

        function load() {
            Permission.forProject(ctrl.project.shortName).findUsersWithRole('ANONYMOUS').then(function (res) {
                var userHasRole = false;

                for (var i = 0; i < res.length; i++) {
                    if (res[i].provider === 'system' && res[i].username === 'anonymous') {
                        userHasRole = true;
                    }
                }
                ctrl.userHasRole = userHasRole;
            });
        }

        function update(newVal) {
            User.byProviderAndUsername('system', 'anonymous').then(function (res) {
                if (newVal) {
                    Permission.forProject(ctrl.project.shortName).addUserToRole(res.id, 'ANONYMOUS').then(load).catch(function () {
                        Notification.addAutoAckNotification('error', {key: 'notification.project-manage-anon.enable.error'}, false);
                    });
                } else {
                    Permission.forProject(ctrl.project.shortName).removeUserToRole(res.id, 'ANONYMOUS').then(load).catch(function () {
                        Notification.addAutoAckNotification('error', {key: 'notification.project-manage-anon.disable.error'}, false);
                    });
                }
            });
        }
    }
}());
