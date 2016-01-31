(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentManageRolePermissions', {
        bindings: {
            roleName: '=',
            roleDesc: '=',
            permissionsByCategory: '=permissions',
            project: '=',
            submit: '=',
            cancel: '='
        },
        controller: ManageRolePermissionsController,
        controllerAs: 'managePermsCtrl',
        templateUrl: 'app/components/common/manage-roles/permissions/permissions.html'
    });

    function ManageRolePermissionsController(Permission, Notification, $modal) {
        var ctrl = this;
        ctrl.view = {};

        //handle manage role at project level:
        var projectName = undefined;
        if(ctrl.project !== undefined) {
            projectName = ctrl.project.shortName;
            Permission = Permission.forProject(projectName);
        }

        var role = ctrl.roleName;

        ctrl.save = function() {
            var permissionsToEnable = [];
            angular.forEach(ctrl.assignStatus, function(value, key) {
                if(value.checked) {
                    permissionsToEnable.push(key);
                }
            });
            Permission.updateRole(role, permissionsToEnable).catch(function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.manage-role.updateRole.error',
                    parameters: {roleName : role}
                }, false);
            }).then(ctrl.submit);
        };

        var hasChanges = function() {
            var result = false;

            //perhaps slower than foreach probably, but avoid traversing the entire object
            for(var key in ctrl.assignStatus) {
                var value = ctrl.assignStatus[key];
                var change = (value.checked != ctrl.hasPermission(key, ctrl.roleDesc.roleAndPermissions));
                if(change) { return true; }
            }
            return false;
        };

        ctrl.cancelWithConfirmation = function() {
            if(!hasChanges()) {
                ctrl.cancel();
                return;
            }

            var modal = $modal.open({
                templateUrl: 'app/components/common/manage-roles/permissions/permissions-confirmation.html',
                controller: function($modalInstance, roleName) {
                    this.roleName = roleName;

                    this.confirm = function() {
                        $modalInstance.close('save');
                    }

                    this.deny = function() {
                        $modalInstance.close('notsave');
                    }

                    this.cancel = function() {
                        $modalInstance.close('cancel');
                    }
                },
                controllerAs: 'confirmCtrl',
                windowClass: 'lavagna-modal',
                size: 'sm',
                resolve: {
                    roleName: function() {
                        return ctrl.roleName;
                    }
                }
            });

            modal.result.then(function(result) {
                if(result === 'save') {
                    ctrl.save();
                }

                if(result === 'notsave') {
                    ctrl.cancel();
                }
            })
        }

        ctrl.assignStatus = {};

        ctrl.hasChanged = function(permission, assignedPermissions, currentStatus) {
            var status = ctrl.hasPermission(permission, assignedPermissions);
            return status != currentStatus;
        }

        /* TODO could remove the linear probe... */
        ctrl.hasPermission = function(permission, assignedPermissions) {
            if(permission == undefined || assignedPermissions == undefined) {
                return;
            }

            for(var i = 0; i<assignedPermissions.length;i++) {
                if(assignedPermissions[i].permission === permission) {
                    return true;
                }
            }
            return false;
        };

        ctrl.hasCategory = function(categoryName) {
            for(var p in ctrl.permissionsByCategory) {
                if(p == categoryName) {
                    return true;
                }
            }
            return false;
        };
    };
})();
