(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AdminManageUsersCtrl', function($modal, $scope, $log, User, UsersAdministration, Admin, Permission, Notification) {

		function loadUsers() {
			User.list().then(function(l) {
				$scope.users = l;
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

				$scope.roles = roleNames;
				configureDefaultUserToAdd();
			});
		}

		function configureDefaultUserToAdd() {
			$scope.userToAdd = {
				provider: $scope.currentUser != undefined ? $scope.currentUser.provider : null,
				username: null,
				email: null,
				displayName: null,
				enabled:true,
				roles: {
					'DEFAULT' : true
				}
			};
		}

		User.currentCachedUser().then(function(u) {
			$scope.currentUser = u;
			$scope.userToAdd.provider = u.provider;
		});

		configureDefaultUserToAdd();

		Admin.findAllLoginHandlers().then(function(loginProviders) {
			$scope.loginProviders = loginProviders;
		});

		loadUsers();
		loadRoles();

		$scope.updateUserStatus = function(userId, enabled) {
			UsersAdministration.toggle(userId, enabled).then(loadUsers, function(error) {
				Notification.addAutoAckNotification('error', {
					key: 'notification.admin-manage-users.toggle.error'
				}, false);
			});
		};

		$scope.addUser = function(userToAdd) {
			var rawRoles = userToAdd.roles;
			var roles = [];
			for(var r in rawRoles) {
				if(rawRoles[r]) {
					roles.push(r);
				}
			}
			userToAdd.roles = roles;
			UsersAdministration.addUser(userToAdd).then(function(data) {
				configureDefaultUserToAdd();
				loadUsers();
			}, function(error) {
				Notification.addAutoAckNotification('error', {
					key: 'notification.admin-manage-users.add.error'
				}, false);
			});
		};

		$scope.importUserFile = null;

		$scope.onFileSelect = function($files) {
			$scope.importUserFile = $files[0]; //single file
		};

		//TODO: progress bar, abort
		$scope.bulkImport = function() {
			Admin.importUsers($scope.importUserFile,
					function(evt) { $log.debug('percent: ' + parseInt(100.0 * evt.loaded / evt.total)); },
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
					function() { $log.debug('aborted'); }).then(loadUsers);
		};

		// ------- user pagination
		$scope.userListPage = 1;

		$scope.switchPage = function(page) {
			$scope.userListPage = page;
		};

        $scope.showUserPermissions = function(user) {
            $modal.open({
                templateUrl: 'partials/admin/fragments/user-permissions-modal.html',
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

    });

})();
