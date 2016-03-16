(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentAdminUsers', {
        bindings: {},
        controller: AdminUserController,
        controllerAs: 'adminUsersCtrl',
        templateUrl: 'app/components/admin/users/users.html'
    });

    function AdminUserController($mdDialog, User, UsersAdministration, Admin, Permission, Notification) {

        var ctrl = this;
        ctrl.view = {};
        ctrl.isOpen = false
        
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

        ctrl.showAddUserDialog = function($event) {
        	$mdDialog.show({
        		templateUrl: 'app/components/admin/users/add-user-dialog.html',
        		controller: function() {        			
        			var ctrl = this;
        			
        			ctrl.close = function () {
                    	$mdDialog.hide();
                    }
        			
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
        		},
        		controllerAs: 'addUserDialogCtrl',
        		bindToController: true,
        		locals: {
        			roles: ctrl.roles,
        			loginProviders : ctrl.loginProviders
        		},
        		targetEvent: $event
        	});
        };
        
        ctrl.showImportDialog = function($event) {
        	$mdDialog.show({
        		templateUrl: 'app/components/admin/users/import-dialog.html',
        		controller: function() {
        			var ctrl = this;
        			
        			ctrl.importUserFile = null;

        	        ctrl.onFileSelect = function($files) {
        	            ctrl.importUserFile = $files[0]; //single file
        	        };
        			
        			ctrl.close = function () {
                    	$mdDialog.hide();
                    }
        			
        			//TODO: progress bar, abort
        	        ctrl.bulkImport = function() {
        	            Admin.importUsers(ctrl.importUserFile,
        	                    function(evt) { },
        	                    function() {
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
        		},
        		controllerAs: 'importDialogCtrl',
        		targetEvent: $event
        	});
        };

        

        // ------- user pagination
        ctrl.view.userListPage = 1;

        ctrl.switchPage = function(page) {
            ctrl.view.userListPage = page;
        };

        ctrl.showUserPermissions = function(user) {
            $mdDialog.show({
                templateUrl: 'app/components/admin/users/user-permissions-modal.html',
                controller: function ($scope) {

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
