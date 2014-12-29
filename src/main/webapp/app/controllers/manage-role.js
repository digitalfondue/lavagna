(function() {

	//shared by admin and project admin

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ManageRoleCtrl', function($scope, $stateParams, Permission, Notification, ProjectCache, StompClient, $modal, $filter) {
		
		//handle manage role at project level:
		var projectName = undefined;
		if($stateParams.projectName !== undefined) {
			projectName = $stateParams.projectName;
			ProjectCache.project(projectName).then(function(res) {
				 $scope.project = res;
			});
			Permission = Permission.forProject(projectName);
		}

		var reloadRoles = function() {
			Permission.findAllRolesAndRelatedPermissions().then(function(res) {
				
				for(var roleName in res) {
					if(res[roleName].hidden) {
						delete res[roleName];
					}
				}
				
				$scope.roles = res;
				for(var roleName in res) {
					$scope.loadUsersWithRole(roleName);
				}
			});
		};

		var reloadUserWithRole = function(roleName) {
			if(angular.isDefined($scope.usersWithRole[roleName])) {
				$scope.loadUsersWithRole(roleName);
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

		$scope.usersWithRole = {};

		$scope.loadUsersWithRole = function(roleName) {
			Permission.findUsersWithRole(roleName).then(function(users) {
				$scope.usersWithRole[roleName] = users;
			});
		};
		
		$scope.roleState = {};

		$scope.createRole = function(roleName) {
			Permission.createRole(roleName).catch(function() {
				Notification.addAutoAckNotification('error', {
					key: 'notification.manage-role.createRole.error'
				}, false);
			});
		};

		$scope.deleteRole = function(roleName) {
			Permission.deleteRole(roleName).catch(function() {
				Notification.addAutoAckNotification('error', {
					key: 'notification.manage-role.deleteRole.error',
					parameters: {roleName : roleName}
				}, false);
			});
		};

		var isRoleAssignedToUser = function(userId, roleName) {
			for(var i = 0; i < $scope.usersWithRole[roleName].length; i++ ) {
				if($scope.usersWithRole[roleName][i].id == userId) {
					return true;
				}
			}
			return false;
		}

		$scope.addUserToRole = function(username, roleName) {
			if(!isRoleAssignedToUser(username.id, roleName)) {
				Permission.addUserToRole(username.id, roleName).catch(function() {
					Notification.addAutoAckNotification('error', {
						key: 'notification.manage-role.addUserToRole.error',
						parameters: {roleName : roleName, userName : $filter('formatUser')(username)}
					}, false);
				});
			}
		};

		$scope.removeUserToRole = function(username, roleName) {
			Permission.removeUserToRole(username, roleName).catch(function() {
				Notification.addAutoAckNotification('error', {
					key: 'notification.manage-role.removeUserToRole.error',
					parameters: {roleName : roleName, userName : $filter('formatUser')(username)}
				}, false);
			});
		};

		reloadRoles();

		Permission.allAvailablePermissionsByCategory().then(function(res) {
			$scope.permissionsByCategory = res;
		});

		$scope.userListPage = 1;
		$scope.switchUserPage = function(page) {
			$scope.userListPage = page;
		};

		//modal
		$scope.open = function (roleName, roleDescriptor) {

			var modalInstance = $modal.open({
				templateUrl: 'partials/fragments/common-manage-roles-modal.html',
				controller: ModalInstanceCtrl,
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
						return $scope.permissionsByCategory;
					}
				}
		    });

		};

		var ModalInstanceCtrl = function ($scope, $modalInstance, role, roleDescriptor, permissionsByCategory) {

			$scope.roleName = role;
			$scope.roleDescriptor = roleDescriptor;
			$scope.permissionsByCategory = permissionsByCategory;

			$scope.save = function() {
				var permissionsToEnable = [];
				angular.forEach($scope.assignStatus, function(value, key) {
					if(value.checked) {
						permissionsToEnable.push(key);
					}
				});
				Permission.updateRole(role, permissionsToEnable).catch(function() {
					Notification.addAutoAckNotification('error', {
						key: 'notification.manage-role.updateRole.error',
						parameters: {roleName : role}
					}, false);
				}).then($scope.cancel);
			};
			
			var hasChanges = function() {
				var result = false;
				
				//perhaps slower than foreach probably, but avoid traversing the entire object
				for(var key in $scope.assignStatus) {
					var value = $scope.assignStatus[key];
					var change = (value.checked != $scope.hasPermission(key, $scope.roleDescriptor.roleAndPermissions));
					if(change) { return true; }
				}
				return false;
			};

			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
			
			$scope.cancelWithConfirmation = function() {
				if(!hasChanges()) {
					$scope.cancel();
					return;
				}
				
				var modal = $modal.open({
					templateUrl: 'partials/fragments/common-manage-roles-modal-confirmation.html',
					controller: function($scope, $modalInstance, roleName) {
						$scope.roleName = roleName;
						
						$scope.confirm = function() {
							$modalInstance.close('save');
						}
						
						$scope.deny = function() {
							$modalInstance.close('notsave');
						}
						
						$scope.cancel = function() {
							$modalInstance.close('cancel');
						}
					},
					windowClass: 'lavagna-modal',
					size: 'sm',
					keyboard: false,
					resolve: {
						roleName: function() {
							return $scope.roleName;
						}
					}
				});
				
				modal.result.then(function(result) {
					if(result === 'save') {
						$scope.save();
					}
					
					if(result === 'notsave') {
						$scope.cancel();
					}
				})
			}

			$scope.assignStatus = {};
			
			$scope.hasChanged = function(permission, assignedPermissions, currentStatus) {
				var status = $scope.hasPermission(permission, assignedPermissions);
				return status != currentStatus;
			}

			/* TODO could remove the linear probe... */
			$scope.hasPermission = function(permission, assignedPermissions) {
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

			$scope.hasCategory = function(categoryName) {
				for(var p in $scope.permissionsByCategory) {
					if(p == categoryName) {
						return true;
					}
				}
				return false;
			};
		};
	});
})();