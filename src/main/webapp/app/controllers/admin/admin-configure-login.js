(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	function getPort(window) {
		return window.location.port || (window.location.protocol === "https:" ? "443" : "80")
	}

	module.controller('AdminConfigureLoginCtrl', function($scope, $window, $modal, Admin, Permission, Notification, User, CONTEXT_PATH, oauthProviders) {
		$scope.oauthProviders = oauthProviders;

		function loadConfiguration() {
			Admin.findAllConfiguration().then(function(conf) {
				$scope.currentConf = conf;

				var authMethods = JSON.parse(conf['AUTHENTICATION_METHOD']);
				$scope.authMethod = {"DEMO": false, "LDAP": false, "PERSONA" : false, "OAUTH" : false};
				for(var i = 0; i < authMethods.length;i++) {
					$scope.authMethod[authMethods[i]] = true;
				}

				$scope.persona = {audience : conf['PERSONA_AUDIENCE'] || ($window.location.protocol + '//' + $window.location.hostname + ':' + getPort($window))};

				$scope.ldap = {
						serverUrl : conf['LDAP_SERVER_URL'],
						managerDn : conf['LDAP_MANAGER_DN'],
						managerPassword : conf['LDAP_MANAGER_PASSWORD'],
						userSearchBase : conf['LDAP_USER_SEARCH_BASE'],
						userSearchFilter: conf['LDAP_USER_SEARCH_FILTER']
				}

				var oauth = conf['OAUTH_CONFIGURATION'] && JSON.parse(conf['OAUTH_CONFIGURATION']) || { providers : []};

				$scope.oauth = {};

				angular.forEach($scope.oauthProviders, function(p) {
					$scope.oauth[p] = {};
				});

				$scope.oauth.baseUrl = oauth.baseUrl || CONTEXT_PATH;


				for(var i = 0; i< oauth.providers.length;i++) {
					$scope.oauth[oauth.providers[i].provider] = oauth.providers[i];
					$scope.oauth[oauth.providers[i].provider].present = true;
				}
				
			});
		}

		loadConfiguration();
		
		// -- enable providers
		
		var updateActiveProviders = function(n, o) {
			if(n == undefined || o == undefined || n == o) {
				return;
			}
			
			var toUpdate = [];

			var activeAuthMethod = [];
			for(var k in $scope.authMethod) {
				if($scope.authMethod[k]) {
					activeAuthMethod.push(k);
				}
			}

			toUpdate.push({first : 'AUTHENTICATION_METHOD', second : JSON.stringify(activeAuthMethod)});
			
			Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).catch(function(error) {
				Notification.addAutoAckNotification('error', { key : 'notification.admin-configure-login.updateActiveProviders.error'}, false);
			}).then(loadConfiguration);
		}
		
		$scope.$watch('authMethod.DEMO', updateActiveProviders);
		
		$scope.$watch('authMethod.LDAP', updateActiveProviders);
		
		$scope.$watch('authMethod.PERSONA', updateActiveProviders);
		
		$scope.$watch('authMethod.OAUTH', updateActiveProviders);
		
		// -- save providers config
		
		$scope.saveLdapConfig = function() {
			var toUpdate = [];
			
			toUpdate.push({first : 'LDAP_SERVER_URL', second : $scope.ldap.serverUrl});
			toUpdate.push({first : 'LDAP_MANAGER_DN', second : $scope.ldap.managerDn});
			toUpdate.push({first : 'LDAP_MANAGER_PASSWORD', second : $scope.ldap.managerPassword});
			toUpdate.push({first : 'LDAP_USER_SEARCH_BASE', second : $scope.ldap.userSearchBase});
			toUpdate.push({first : 'LDAP_USER_SEARCH_FILTER', second : $scope.ldap.userSearchFilter});
			
			Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).then(function() {
				Notification.addAutoAckNotification('success', { key : 'notification.admin-configure-login.saveLdapConfig.success'}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', { key : 'notification.admin-configure-login.saveLdapConfig.error'}, false);
			}).then(loadConfiguration);
		};
		
		$scope.savePersonaConfig = function() {
			var toUpdate = [];
			
			toUpdate.push({first : 'PERSONA_AUDIENCE', second : $scope.persona.audience});
			
			Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).then(function() {
				Notification.addAutoAckNotification('success', { key : 'notification.admin-configure-login.savePersonaConfig.success'}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', { key : 'notification.admin-configure-login.savePersonaConfig.error'}, false);
			}).then(loadConfiguration);
		};
		
		function addProviderIfPresent(list, conf, provider) {
			if(conf && conf.present) {
				list.push({provider: provider, apiKey: conf.apiKey, apiSecret : conf.apiSecret});
			}
		}
		
		$scope.saveOauthConfig = function() {
			var toUpdate = [];
			
			var newOauthConf = {baseUrl: $scope.oauth.baseUrl, providers : []};
			angular.forEach($scope.oauthProviders, function(provider) {
				addProviderIfPresent(newOauthConf.providers, $scope.oauth[provider], provider);
			});
			toUpdate.push({first : 'OAUTH_CONFIGURATION', second : JSON.stringify(newOauthConf)});
			
			Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).then(function() {
				Notification.addAutoAckNotification('success', { key : 'notification.admin-configure-login.saveOAuthConfig.success'}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', { key : 'notification.admin-configure-login.saveOAuthConfig.error'}, false);
			}).then(loadConfiguration);
		};
		
		$scope.openLdapConfigModal = function() {
			var modalInstance = $modal.open({
				templateUrl: 'partials/admin/fragments/ldap-check-modal.html',
				controller: function($scope, $modalInstance, ldapConfig) {
					$scope.checkLdapConfiguration = function(ldapConf, ldapCheck) {
						Admin.checkLdap(ldapConf, ldapCheck).then(function(r) {
							$scope.ldapCheckResult = r;
						});
					};
					
					$scope.close = function() {
						$modalInstance.close('done');
					}
				},
				size: 'sm',
				windowClass: 'lavagna-modal',
				resolve: {
					ldapConfig: function () {
						return $scope.ldap;
					}
				}
		    });
		}
		
		// ------ Anonymous access
		
		var load = function () {
			Admin.findByKey('ENABLE_ANON_USER').then(function (res) {
				$scope.anonEnabled = res.second === "true";
			});

			Permission.findUsersWithRole('ANONYMOUS').then(function (res) {
				var userHasGlobalRole = false;
				for (var i = 0; i < res.length; i++) {
					if (res[i].provider === 'system' && res[i].username === 'anonymous') {
						userHasGlobalRole = true;
					}
				}
				$scope.userHasGlobalRole = userHasGlobalRole;
			});
			
			Permission.findAllRolesAndRelatedPermissions().then(function(res) {
				var userHasSearchPermission = false;
				for(var i = 0; i < res['ANONYMOUS'].roleAndPermissions.length; i++) {
					var permission = res['ANONYMOUS'].roleAndPermissions[i];
					if(permission.permission === 'SEARCH') {
						userHasSearchPermission = true;
					}
				}
				$scope.userHasSearchPermission = userHasSearchPermission;
			});
			
		};

		load();
		
		$scope.$watch('anonEnabled', function(newVal, oldVal) {
			if(newVal == undefined || oldVal == undefined || newVal == oldVal) {
				return;
			}
			Admin.updateConfiguration(
					{toUpdateOrCreate: 
						[{first: 'ENABLE_ANON_USER', second: newVal.toString()}]}
				).catch(function(error) {
					Notification.addAutoAckNotification('error', { key : 'notification.admin-configure-login.updateAnonConfiguration.error'}, false);
				}).then(load);
		});
		
		$scope.$watch('userHasGlobalRole', function(newVal, oldVal) {
			if(newVal == undefined || oldVal == undefined || newVal == oldVal) {
				return;
			}
			User.byProviderAndUsername('system', 'anonymous').then(function (res) {
				if (newVal) {
					Permission.addUserToRole(res.id, 'ANONYMOUS').then(load);
				} else {
					Permission.removeUserToRole(res.id, 'ANONYMOUS').then(load);
				}
			})
		});
		
		$scope.$watch('userHasSearchPermission', function(newVal, oldVal) {
			if(newVal == undefined || oldVal == undefined || newVal == oldVal) {
				return;
			}
			
			Permission.toggleSearchPermissionForAnonymousUsers(newVal).then(load);
		});
	});
})();
