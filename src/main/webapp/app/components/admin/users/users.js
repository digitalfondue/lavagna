(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminUsers', {
        templateUrl: 'app/components/admin/users/users.html',
        controller: ['$mdDialog', 'User', 'UsersAdministration', 'Admin', 'Permission', 'Notification', AdminUserController]
    });

    function AdminUserController($mdDialog, User, UsersAdministration, Admin, Permission, Notification) {
        var ctrl = this;
        //

        ctrl.updateUserStatus = updateUserStatus;
        ctrl.showAddUserDialog = showAddUserDialog;
        ctrl.showImportDialog = showImportDialog;
        ctrl.showUserPermissions = showUserPermissions;
        ctrl.editUserInfo = editUser;
        ctrl.resetPassword = resetPassword;
        ctrl.userStatusFilter = true;
        //

        ctrl.$onInit = function init() {
            ctrl.view = {};
            ctrl.isOpen = false;
            loadCurrentUser();
            Admin.findAllLoginHandlers().then(function (loginProviders) {
                ctrl.loginProviders = loginProviders;
            });
            loadUsers();
            loadRoles();
        };

        function loadUsers() {
            User.list().then(function (l) {
                ctrl.users = l;
            });
        }

        function loadCurrentUser() {
            User.currentCachedUser().then(function (user) {
                ctrl.currentUser = user;
            });
        }

        function loadRoles() {
            Permission.findAllRolesAndRelatedPermissions().then(function (res) {
                var roleNames = [];

                for (var roleName in res) {
                    if (res.hasOwnProperty(roleName) && !res[roleName].hidden) {
                        roleNames.push(roleName);
                    }
                }

                ctrl.roles = roleNames;
                configureDefaultUserToAdd();
            });
        }

        function configureDefaultUserToAdd() {
            ctrl.view.userToAdd = {
                provider: angular.isDefined(ctrl.currentUser) ? ctrl.currentUser.provider : null,
                username: null,
                email: null,
                displayName: null,
                enabled: true,
                roles: {
                    'DEFAULT': true
                }
            };
        }

        function updateUserStatus(userId, enabled) {
            UsersAdministration.toggle(userId, enabled).then(loadUsers, function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-manage-users.toggle.error'
                }, false);
            });
        }

        function showAddUserDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/admin/users/add-user-dialog.html',
                controller: function () {
                    var ctrl = this;

                    ctrl.close = function () {
                        $mdDialog.hide();
                    };

                    ctrl.addUser = function (userToAdd) {
                        if (userToAdd.provider !== 'password') {
                            userToAdd.password = null;
                        }

                        UsersAdministration.addUser(userToAdd).then(function () {
                            configureDefaultUserToAdd();
                            loadUsers();
                            $mdDialog.hide();
                        }, function () {
                            Notification.addAutoAckNotification('error', {
                                key: 'notification.admin-manage-users.add.error'
                            }, false);
                        });
                    };
                },
                controllerAs: 'addUserDialogCtrl',
                bindToController: true,
                locals: {
                    roles: ctrl.roles,
                    loginProviders: ctrl.loginProviders
                }
            });
        }

        function editUser(user) {
            showEditUserDialog(user).then(function (userToEdit) {
                return UsersAdministration.editUser(userToEdit).then(function () {
                    Notification.addAutoAckNotification('success', {
                        key: 'notification.admin-manage-users.edit.success'
                    }, false);

                    return loadUsers();
                }, function () {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.admin-manage-users.edit.error'
                    }, false);
                });
            });
        }

        function resetPassword(user) {
            showResetPasswordDialog(user).then(function (password) {
                return UsersAdministration.resetPassword(user.id, password).then(function () {
                    Notification.addAutoAckNotification('success', {
                        key: 'notification.admin-manage-users.password.success'
                    }, false);
                }, function () {
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.admin-manage-users.password.error'
                    }, false);
                });
            });
        }

        function showEditUserDialog(user) {
            return $mdDialog.show({
                templateUrl: 'app/components/admin/users/edit-user-dialog.html',
                bindToController: true,
                locals: {
                    userToEdit: angular.copy(user)
                },
                controller: function () {
                    var ctrl = this;

                    ctrl.cancel = function () {
                        $mdDialog.cancel();
                    };

                    ctrl.save = function () {
                        $mdDialog.hide(ctrl.userToEdit);
                    };
                },
                controllerAs: 'editUserDialogCtrl'
            });
        }

        function showResetPasswordDialog(user) {
            return $mdDialog.show({
                templateUrl: 'app/components/admin/users/reset-password-dialog.html',
                bindToController: true,
                locals: {
                    userToEdit: user
                },
                controller: function () {
                    var ctrl = this;

                    ctrl.cancel = function () {
                        $mdDialog.cancel();
                    };

                    ctrl.reset = function (password, confirmPassword) {
                        if (password !== confirmPassword) {
                            return false;
                        }

                        $mdDialog.hide(password);
                    };
                },
                controllerAs: 'resetPasswordDialogCtrl'
            });
        }

        function showImportDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/admin/users/import-dialog.html',
                controller: function () {
                    var ctrl = this;

                    var uploader = ctrl.uploader = Admin.getImportUsersUploader();

                    ctrl.file = null;

                    uploader.onAfterAddingFile = function (fileItem) {
                        ctrl.file = fileItem;
                    };

                    var reload = function () {
                        ctrl.file = null;
                        uploader.clearQueue();
                        loadUsers();
                    };

                    uploader.onSuccessItem = function () {
                        Notification.addAutoAckNotification('success', {
                            key: 'notification.admin-manage-users.bulkImport.success'
                        }, false);
                        reload();
                    };
                    uploader.onErrorItem = function () {
                        Notification.addAutoAckNotification('error', {
                            key: 'notification.admin-manage-users.bulkImport.error'
                        }, false);
                        reload();
                    };

                    ctrl.close = function () {
                        $mdDialog.hide();
                    };
                },
                controllerAs: 'importDialogCtrl'
            });
        }

        function showUserPermissions(user) {
            $mdDialog.show({
                templateUrl: 'app/components/admin/users/user-permissions-modal.html',
                controller: function ($scope) {
                    $scope.user = user;

                    Permission.findUserRoles(user.id).then(function (rolesByProject) {
                        $scope.rolesByProject = rolesByProject;
                    });

                    $scope.close = function () {
                        $mdDialog.hide();
                    };
                }
            });
        }
    }
}());
