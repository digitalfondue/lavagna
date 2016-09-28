(function() {
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
        //
        
        ctrl.$onInit = function init() {
        	ctrl.view = {};
            ctrl.isOpen = false
            loadCurrentUser();
            Admin.findAllLoginHandlers().then(function(loginProviders) {
                ctrl.loginProviders = loginProviders;
            });
            loadUsers();
            loadRoles();
        };

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

        function updateUserStatus(userId, enabled) {
            UsersAdministration.toggle(userId, enabled).then(loadUsers, function(error) {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-manage-users.toggle.error'
                }, false);
            });
        };

        function showAddUserDialog($event) {
        	$mdDialog.show({
        		templateUrl: 'app/components/admin/users/add-user-dialog.html',
        		controller: function() {
        			var ctrl = this;

        			ctrl.close = function () {
                    	$mdDialog.hide();
                    }

        			ctrl.addUser = function(userToAdd) {
        	            UsersAdministration.addUser(userToAdd).then(function() {
        	                configureDefaultUserToAdd();
        	                loadUsers();
        	                $mdDialog.hide();
        	            }, function(error) {
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
        			loginProviders : ctrl.loginProviders
        		},
        	});
        };

        function showImportDialog($event) {
        	$mdDialog.show({
        		templateUrl: 'app/components/admin/users/import-dialog.html',
        		controller: function() {
        			var ctrl = this;

        			var uploader = ctrl.uploader = Admin.getImportUsersUploader();

        			ctrl.file = null;

        			uploader.onAfterAddingFile = function(fileItem) {
        			    ctrl.file = fileItem;
        			};

        			var reload = function() {
        			    ctrl.file = null;
        			    uploader.clearQueue();
        			    loadUsers();
        			}

        			uploader.onSuccessItem = function(fileItem, response, status, headers) {
                        Notification.addAutoAckNotification('success', {
                            key: 'notification.admin-manage-users.bulkImport.success'
                        }, false);
                        reload();
                    };
                    uploader.onErrorItem = function(fileItem, response, status, headers) {
                        Notification.addAutoAckNotification('error', {
                            key: 'notification.admin-manage-users.bulkImport.error'
                        }, false);
                        reload();
                    };

        			ctrl.close = function () {
                    	$mdDialog.hide();
                    }
        		},
        		controllerAs: 'importDialogCtrl',
        	});
        };

        function showUserPermissions(user) {
            $mdDialog.show({
                templateUrl: 'app/components/admin/users/user-permissions-modal.html',
                controller: function ($scope) {

                	$scope.user = user;
                	
                    Permission.findUserRoles(user.id).then(function(rolesByProject) {
                        $scope.rolesByProject = rolesByProject;
                    });

                    $scope.close = function () {
                    	$mdDialog.hide();
                    }
                }
            });
        };

    }
})();
