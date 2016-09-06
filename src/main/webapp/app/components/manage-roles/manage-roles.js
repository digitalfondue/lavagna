(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgManageRoles', {
        bindings: {
            project: '='
        },
        controller: ['$scope', 'Permission', 'Notification', 'ProjectCache', 'StompClient', 'User', '$mdDialog', '$filter', '$translate', ManageRolesController],
        templateUrl: 'app/components/manage-roles/manage-roles.html'
    });

    function ManageRolesController($scope, Permission, Notification, ProjectCache, StompClient, User, $mdDialog, $filter, $translate) {

        var ctrl = this;
        ctrl.isGlobalRoles = ctrl.project === undefined;
        User.currentCachedUser().then(function(user) {
        	ctrl.currentUser = user;
        });

        //handle manage role at project level:
        var projectName = undefined;
        if(ctrl.project !== undefined) {
            projectName = ctrl.project.shortName;
            Permission = Permission.forProject(projectName);
        }
        
        
         

        var getUsersByRole = function(roleName, users, limit) {
            users = $filter('filterUsersBy')(users, ctrl.userFilterText);
            return users.slice(0, 40);
        };

        ctrl.filterUsersBy = function() {
            angular.forEach(ctrl.usersByRole, function(users, name) {
                ctrl.usersWithRole[name] = getUsersByRole(name, users, 40);
            });
        };

        ctrl.searchUser = function(text) {
			return User.findUsersGlobally(text.trim()).then(function (res) {
				angular.forEach(res, function(user) {
					user.label = User.formatName(user);
				});
				return res;
			});
		};

        var reloadRoles = function() {
            Permission.findAllRolesAndRelatedPermissions().then(function(res) {

                for(var roleName in res) {getUsersByRole
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
        StompClient.subscribe(route , function(event) {
            var b = JSON.parse(event.body);
            if(b.type === 'REMOVE_ROLE_TO_USERS' || b.type === 'ASSIGN_ROLE_TO_USERS') {
                reloadUserWithRole(b.payload);
            } else {
                reloadRoles();
            }
        }, $scope);

        ctrl.usersByRole = {};
        ctrl.usersWithRole = {};

        var loadUsersWithRole = function(roleName) {
            Permission.findUsersWithRole(roleName).then(function(users) {
                ctrl.usersByRole[roleName] = users;
                ctrl.usersWithRole[roleName] = getUsersByRole(roleName, users, 40);
            });
        };

        ctrl.deleteRole = function(roleName, ev) {
            var confirm = $mdDialog.confirm()
                  .title($translate.instant('manage.roles.delete.dialog.title'))
                  .textContent($translate.instant('manage.roles.delete.dialog.message', {name: roleName}))
                  .targetEvent(ev)
                  .ok($translate.instant('button.yes'))
                  .cancel($translate.instant('button.no'));
            $mdDialog.show(confirm).then(function() {
              return Permission.deleteRole(roleName);
            }).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.manage-role.deleteRole.success', parameters: {roleName : roleName}}, false);
            }, function(error) {
                if(error) {
                    Notification.addAutoAckNotification('error', {key: 'notification.manage-role.deleteRole.error', parameters: {roleName : roleName}}, false);
                }
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

        ctrl.addUserToRole = function(user, roleName) {
            if(!isRoleAssignedToUser(user.id, roleName)) {
                Permission.addUserToRole(user.id, roleName).catch(function() {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.manage-role.addUserToRole.error',
                        parameters: {roleName : roleName, userName : $filter('formatUser')(user)}
                    }, false);
                });
            }
        };

        ctrl.removeUserToRole = function(user, roleName) {
            Permission.removeUserToRole(user.id, roleName).catch(function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.removeUserToRole.error',
                    parameters: {roleName : roleName, userName : $filter('formatUser')(user)}
                }, false);
            });
        };

        reloadRoles();

        Permission.allAvailablePermissionsByCategory().then(function(res) {
            ctrl.permissionsByCategory = res;
        });

        //permission modal
        ctrl.open = function(roleName, roleDescriptor) {
            var dialog = $mdDialog.show({
            	autoWrap: false,
                templateUrl: 'app/components/manage-roles/permissions/manage-role-permissions-dialog.html',
                controller: function(role, roleDescriptor, permissionsByCategory, project) {
                    this.roleName = role;
                    this.roleDesc = roleDescriptor;
                    this.perms = permissionsByCategory;
                    this.project = project;

                    this.submit = function($permissionsToEnable) {
                    	$mdDialog.hide($permissionsToEnable);
                    };

                    this.cancel = function() {
                    	$mdDialog.cancel(false);
                    };
                },
                controllerAs: 'modalResolver',
                fullscreen: true,
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

            dialog.then(function (permissionsToEnable) {
                return Permission.updateRole(roleName, permissionsToEnable);
            }).catch(function (error) {
                if(error) {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.manage-role.updateRole.error',
                        parameters: {roleName : roleName}
                    }, false);
                }
            });
        }

        //
        ctrl.showAddRoleDialog = function($event) {
        	$mdDialog.show({
        		templateUrl: 'app/components/manage-roles/add-role-dialog.html',
        		targetEvent: $event,
        		controller: function() {
        			var ctrl = this;
        			ctrl.createRole = createRole;

        			ctrl.close = function() {
                    	$mdDialog.hide();
                    };
        		},
        		controllerAs: 'addRoleDialogCtrl'
        	});
        }

        function createRole(roleName) {
            Permission.createRole(roleName).catch(function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.createRole.error'
                }, false);
            });
        };
    };
})();
