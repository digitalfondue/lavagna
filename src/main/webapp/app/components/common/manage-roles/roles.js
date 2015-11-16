(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgComponentManageRoles', ManageRolesComponent);

    function ManageRolesComponent(Permission, Notification, ProjectCache, StompClient, $modal, $filter) {
        return {
            restrict: 'E',
            scope: true,
            bindToController: {
                project: '='
            },
            controller: ManageRolesController,
            controllerAs: 'manageRolesCtrl',
            templateUrl: 'app/components/common/manage-roles/roles.html'
        }
    };

    function ManageRolesController($scope, Permission, Notification, ProjectCache, StompClient, $modal, $filter) {

        var ctrl = this;
        ctrl.view = {};
        ctrl.view.roleState = {};

        //handle manage role at project level:
        var projectName = undefined;
        if(ctrl.project !== undefined) {
            projectName = ctrl.project.shortName;
            Permission = Permission.forProject(projectName);
        }

        var reloadRoles = function() {
            Permission.findAllRolesAndRelatedPermissions().then(function(res) {

                for(var roleName in res) {
                    if(res[roleName].hidden) {
                        delete res[roleName];
                    }
                }

                ctrl.roles = res;
                for(var roleName in res) {
                    loadUsersWithRole(roleName);
                }
            });
        };

        var reloadUserWithRole = function(roleName) {
            if(angular.isDefined(ctrl.usersWithRole[roleName])) {
                loadUsersWithRole(roleName);
            }
        };

        var route = '/event/permission' + (projectName === undefined ? '' : ('/project/'+ projectName));
        StompClient.subscribe($scope, route , function(event) {
            var b = JSON.parse(event.body);
            if(b.type === 'REMOVE_ROLE_TO_USERS' || b.type === 'ASSIGN_ROLE_TO_USERS') {
                reloadUserWithRole(b.payload);
            } else {
                reloadRoles();
            }
        });

        ctrl.usersWithRole = {};

        var loadUsersWithRole = function(roleName) {
            Permission.findUsersWithRole(roleName).then(function(users) {
                ctrl.usersWithRole[roleName] = users;
            });
        };

        ctrl.createRole = function(roleName) {
            Permission.createRole(roleName).catch(function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.createRole.error'
                }, false);
            });
        };

        ctrl.deleteRole = function(roleName) {
            Permission.deleteRole(roleName).catch(function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.deleteRole.error',
                    parameters: {roleName : roleName}
                }, false);
            });
        };

        var isRoleAssignedToUser = function(userId, roleName) {
            for(var i = 0; i < ctrl.usersWithRole[roleName].length; i++ ) {
                if(ctrl.usersWithRole[roleName][i].id == userId) {
                    return true;
                }
            }
            return false;
        }

        ctrl.addUserToRole = function(username, roleName) {
            if(!isRoleAssignedToUser(username.id, roleName)) {
                Permission.addUserToRole(username.id, roleName).catch(function() {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.manage-role.addUserToRole.error',
                        parameters: {roleName : roleName, userName : $filter('formatUser')(username)}
                    }, false);
                });
            }
        };

        ctrl.removeUserToRole = function(username, roleName) {
            Permission.removeUserToRole(username, roleName).catch(function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.removeUserToRole.error',
                    parameters: {roleName : roleName, userName : $filter('formatUser')(username)}
                }, false);
            });
        };

        reloadRoles();

        Permission.allAvailablePermissionsByCategory().then(function(res) {
            ctrl.permissionsByCategory = res;
        });

        ctrl.view.userListPage = 1;
        ctrl.switchUserPage = function(page) {
            ctrl.view.userListPage = page;
        };

        //permission modal
        ctrl.open = function(roleName, roleDescriptor) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/common/manage-roles/permissions/permissions-modal.html',
                controller: function($modalInstance, role, roleDescriptor, permissionsByCategory, project) {
                    this.roleName = role;
                    this.roleDesc = roleDescriptor;
                    this.perms = permissionsByCategory;
                    this.project = project;

                    this.submit = function() {
                        $modalInstance.close();
                    }

                    this.close = function() {
                        $modalInstance.close();
                    }

                    this.cancel = function() {
                        $modalInstance.close();
                    }
                },
                controllerAs: 'modalResolver',
                size: 'lg',
                windowClass: 'lavagna-modal',
                keyboard: false,
                backdrop: 'static',
                resolve: {
                    role: function () {
                        return roleName;
                    },
                    roleDescriptor : function() {
                        return roleDescriptor;
                    },
                    permissionsByCategory : function() {
                        return ctrl.permissionsByCategory;
                    },
                    project: function() {
                        return ctrl.project;
                    }
                }
            });
        }
    };
})();
