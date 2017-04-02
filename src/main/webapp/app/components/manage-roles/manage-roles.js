(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgManageRoles', {
        bindings: {
            project: '='
        },
        controller: ['Permission', 'Notification', 'ProjectCache', 'StompClient', 'User', '$mdDialog', '$filter', '$translate', ManageRolesController],
        templateUrl: 'app/components/manage-roles/manage-roles.html'
    });

    function ManageRolesController(Permission, Notification, ProjectCache, StompClient, User, $mdDialog, $filter, $translate) {
        var ctrl = this;

        //
        ctrl.filterUsersBy = filterUsersBy;
        ctrl.searchUser = searchUser;
        ctrl.deleteRole = deleteRole;
        ctrl.addUserToRole = addUserToRole;
        ctrl.removeUserToRole = removeUserToRole;
        ctrl.open = open;
        ctrl.showAddRoleDialog = showAddRoleDialog;
        //

        var stompDestroy = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.isGlobalRoles = ctrl.project === undefined;

            User.currentCachedUser().then(function (user) {
                ctrl.currentUser = user;
            });

            // handle manage role at project level:
            var projectName;

            if (ctrl.project !== undefined) {
                projectName = ctrl.project.shortName;
                ctrl.Permission = Permission.forProject(projectName);
            } else {
                ctrl.Permission = Permission;
            }
            //

            var route = '/event/permission' + (projectName === undefined ? '' : ('/project/' + projectName));

            stompDestroy = StompClient.subscribe(route, function (event) {
                var b = JSON.parse(event.body);

                if (b.type === 'REMOVE_ROLE_TO_USERS' || b.type === 'ASSIGN_ROLE_TO_USERS') {
                    reloadUserWithRole(b.payload);
                } else {
                    reloadRoles();
                }
            });

            ctrl.usersByRole = {};
            ctrl.usersWithRole = {};

            reloadRoles();

            ctrl.Permission.allAvailablePermissionsByCategory().then(function (res) {
                ctrl.permissionsByCategory = res;
            });
        };

        ctrl.$onDestroy = function () {
            stompDestroy();
        };

        //

        function filterUsersByText(users) {
            return $filter('filterUsersBy')(users, ctrl.userFilterText).slice(0, 40);
        }

        function filterUsersBy() {
            angular.forEach(ctrl.usersByRole, function (users, name) {
                ctrl.usersWithRole[name] = filterUsersByText(users);
            });
        }

        function searchUser(text) {
            return User.findUsersGlobally(text.trim()).then(function (res) {
                angular.forEach(res, function (user) {
                    user.label = User.formatName(user);
                });

                return res;
            });
        }

        function reloadRoles() {
            ctrl.Permission.findAllRolesAndRelatedPermissions().then(function (res) {
                for (var roleName in res) {
                    if (res.hasOwnProperty(roleName) && res[roleName].hidden) {
                        delete res[roleName];
                    }
                }

                ctrl.roles = res;
                for (var remainingRoleName in res) {
                    if (res.hasOwnProperty(remainingRoleName)) {
                        loadUsersWithRole(remainingRoleName);
                    }
                }
            });
        }

        function reloadUserWithRole(roleName) {
            if (angular.isDefined(ctrl.usersWithRole[roleName])) {
                loadUsersWithRole(roleName);
            }
        }

        function loadUsersWithRole(roleName) {
            ctrl.Permission.findUsersWithRole(roleName).then(function (users) {
                ctrl.usersByRole[roleName] = users;
                ctrl.usersWithRole[roleName] = filterUsersByText(users);
            });
        }

        function deleteRole(roleName, ev) {
            var confirm = $mdDialog.confirm()
                  .title($translate.instant('manage.roles.delete.dialog.title'))
                  .textContent($translate.instant('manage.roles.delete.dialog.message', {name: roleName}))
                  .targetEvent(ev)
                  .ok($translate.instant('button.yes'))
                  .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                return ctrl.Permission.deleteRole(roleName);
            }).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.manage-role.deleteRole.success', parameters: {roleName: roleName}}, false);
            }, function (error) {
                if (error) {
                    Notification.addAutoAckNotification('error', {key: 'notification.manage-role.deleteRole.error', parameters: {roleName: roleName}}, false);
                }
            });
        }

        function isRoleAssignedToUser(userId, roleName) {
            for (var i = 0; i < ctrl.usersWithRole[roleName].length; i++ ) {
                if (ctrl.usersWithRole[roleName][i].id === userId) {
                    return true;
                }
            }

            return false;
        }

        function addUserToRole(user, roleName) {
            if (!isRoleAssignedToUser(user.id, roleName)) {
                ctrl.Permission.addUserToRole(user.id, roleName).catch(function () {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.manage-role.addUserToRole.error',
                        parameters: {roleName: roleName, userName: $filter('formatUser')(user)}
                    }, false);
                });
            }
        }

        function removeUserToRole(user, roleName) {
            ctrl.Permission.removeUserToRole(user.id, roleName).catch(function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.removeUserToRole.error',
                    parameters: {roleName: roleName, userName: $filter('formatUser')(user)}
                }, false);
            });
        }

        // permission modal
        function open(roleName, roleDescriptor) {
            var dialog = $mdDialog.show({
                autoWrap: false,
                templateUrl: 'app/components/manage-roles/permissions/manage-role-permissions-dialog.html',
                controller: function (role, roleDescriptor, permissionsByCategory, project) {
                    this.roleName = role;
                    this.roleDesc = roleDescriptor;
                    this.perms = permissionsByCategory;
                    this.project = project;

                    this.submit = function ($permissionsToEnable) {
                        $mdDialog.hide($permissionsToEnable);
                    };

                    this.cancel = function () {
                        $mdDialog.cancel(false);
                    };
                },
                controllerAs: 'modalResolver',
                fullscreen: true,
                resolve: {
                    role: function () {
                        return roleName;
                    },
                    roleDescriptor: function () {
                        return roleDescriptor;
                    },
                    permissionsByCategory: function () {
                        return ctrl.permissionsByCategory;
                    },
                    project: function () {
                        return ctrl.project;
                    }
                }
            });

            dialog.then(function (permissionsToEnable) {
                return ctrl.Permission.updateRole(roleName, permissionsToEnable);
            }).catch(function (error) {
                if (error) {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.manage-role.updateRole.error',
                        parameters: {roleName: roleName}
                    }, false);
                }
            });
        }

        //
        function showAddRoleDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/manage-roles/add-role-dialog.html',
                controller: function () {
                    var ctrl = this;

                    ctrl.createRole = createRole;

                    ctrl.close = function () {
                        $mdDialog.hide();
                    };
                },
                controllerAs: 'addRoleDialogCtrl'
            });
        }

        function createRole(roleName) {
            ctrl.Permission.createRole(roleName).catch(function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.createRole.error'
                }, false);
            });
        }
    }
}());
