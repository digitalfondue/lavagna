(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentAdminUsers', {
        bindings: {},
        controller: AdminUserController,
        controllerAs: 'adminUsersCtrl',
        templateUrl: 'app/components/admin/users/users.html'
    });

    function AdminUserController($modal, User, UsersAdministration, Admin, Permission, Notification) {

        var ctrl = this;
        ctrl.view = {};

        function loadUsers() {
            User.list().then(function(l) {
                ctrl.users = l;
            });
        }

        function loadCurrentUser() {
            User.currentCachedUser().then(function(user) {
                ctrl.currentUser = user;
            });
        }

        loadCurrentUser();

        function loadRoles() {
            Permission.findAllRolesAndRelatedPermissions().then(function(res) {
                var roleNames = [];
                for(var roleName in res) {
                    if(!res[roleName].hidden) {
                        roleNames.push(roleName);
                    }
                }

                ctrl.roles = roleNames;
                configureDefaultUserToAdd();
            });
        }

        function configureDefaultUserToAdd() {
            ctrl.view.userToAdd = {
                provider: ctrl.currentUser != undefined ? ctrl.currentUser.provider : null,
                username: null,
                email: null,
                displayName: null,
                enabled:true,
                roles: {
                    'DEFAULT' : true
                }
            };
        }

        Admin.findAllLoginHandlers().then(function(loginProviders) {
            ctrl.loginProviders = loginProviders;
        });

        loadUsers();
        loadRoles();

        ctrl.updateUserStatus = function(userId, enabled) {
            UsersAdministration.toggle(userId, enabled).then(loadUsers, function(error) {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-manage-users.toggle.error'
                }, false);
            });
        };

        ctrl.addUser = function(userToAdd) {
            var rawRoles = userToAdd.roles;
            var roles = [];
            for(var r in rawRoles) {
                if(rawRoles[r]) {
                    roles.push(r);
                }
            }
            userToAdd.roles = roles;
            UsersAdministration.addUser(userToAdd).then(function() {
                configureDefaultUserToAdd();
                loadUsers();
            }, function(error) {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-manage-users.add.error'
                }, false);
            });
        };

        ctrl.importUserFile = null;

        ctrl.onFileSelect = function($files) {
            ctrl.importUserFile = $files[0]; //single file
        };

        //TODO: progress bar, abort
        ctrl.bulkImport = function() {
            Admin.importUsers(ctrl.importUserFile,
                    function(evt) { },
                    function(data) {
                        Notification.addAutoAckNotification('success', {
                            key: 'notification.admin-manage-users.bulkImport.success'
                        }, false);
                    },
                    function() {
                        Notification.addAutoAckNotification('error', {
                            key: 'notification.admin-manage-users.bulkImport.error'
                        }, false);
                    },
                    function() { }).then(loadUsers);
        };

        // ------- user pagination
        ctrl.view.userListPage = 1;

        ctrl.switchPage = function(page) {
            ctrl.view.userListPage = page;
        };

        ctrl.showUserPermissions = function(user) {
            $modal.open({
                templateUrl: 'app/components/admin/users/user-permissions-modal.html',
                controller: function ($scope, $modalInstance) {

                    Permission.findUserRoles(user.id).then(function(rolesByProject) {
                        $scope.rolesByProject = rolesByProject;
                    });

                    $scope.close = function () {
                        $modalInstance.close('done');
                    }
                },
                size: 'sm',
                windowClass: 'lavagna-modal'
            });
        };

    }
})();
