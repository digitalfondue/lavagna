(function () {
	'use strict';

	function addProviderIfPresent(list, conf, provider) {
		if (conf && conf.present) {
			list.push({provider: provider, apiKey: conf.apiKey, apiSecret: conf.apiSecret});
			return true;
		}
		return false;
	}

	function getOrigin(window) {
		if (!window.location.origin) {
			window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
		}
		return window.location.origin;
	}

	function getPort(window) {
		return window.location.port || (window.location.protocol === "https:" ? "443" : "80")
	}

	var module = angular.module('lavagna-setup', ['ui.router']);

	module.config(function ($stateProvider, $urlRouterProvider, $httpProvider) {
		$stateProvider.state('first-step', {
			url: '/',
			templateUrl: 'first-step.html',
			controller: 'SetupCtrl'
		}).state('second-step', {
			url: '/login',
			templateUrl: 'second-step.html',
			controller: 'SetupLoginCtrl'
		}).state('third-step', {
			url: '/user',
			templateUrl: 'third-step.html',
			controller: 'SetupUserCtrl'
		}).state('fourth-step', {
			url: '/confirmation',
			templateUrl: 'fourth-step.html',
			controller: 'SetupActivationCtrl'
		});

		$urlRouterProvider.otherwise('/');


		$httpProvider.interceptors.push(function () {
			var csrfToken = null;
			return {
				'request': function (config) {
					if (csrfToken) {
						config.headers['x-csrf-token'] = csrfToken;
					}
					return config;
				},
				'response': function (response) {
					if (response.headers()['x-csrf-token']) {
						csrfToken = response.headers()['x-csrf-token'];
						window.csrfToken = csrfToken;
					}
					return response;
				}
			};
		});
	});

	module.run(function ($rootScope, $state, $window) {


		$rootScope.back = function (state) {
			$state.go(state);
		}


		$rootScope.ldap = {};
		$rootScope.persona = {audience: $window.location.protocol + '//' + $window.location.hostname + ':' + getPort($window)};
		$rootScope.oauth = {baseUrl: getOrigin($window) + $window.location.pathname.replace(/setup\/$/, '')};
		$rootScope.oauthProviders = ['bitbucket', 'github', 'google', 'twitter'];
		angular.forEach($rootScope.oauthProviders, function (p) {
			$rootScope.oauth[p] = {present: false};
		});

		$rootScope.toSave = {first: undefined, second: undefined, user: undefined};


		//redirect to first page if the user is bypassing it (as we are using the rootscope for propagating the values)
		if (!$rootScope.fromFirstStep) {
			$state.go('first-step');
		}
	});

	function goToRootApp() {
		window.location.href = getOrigin(window) + window.location.pathname.replace(/setup\/$/, '');
	}

	module.controller('SetupCtrl', function ($window, $rootScope, $scope, $http, $state) {

		$rootScope.fromFirstStep = true;

		$scope.baseUrlPlaceholder = getOrigin($window) + window.location.pathname.replace(/setup\/$/, '');

		if ($rootScope.toSave.first) {
			$scope.baseUrl = $rootScope.toSave.first[0].second;
		} else {
			$scope.baseUrl = $scope.baseUrlPlaceholder;
		}


		$http.get('').then(function () {
			$scope.submitImport = function () {
				var fd = new FormData();
				fd.append("overrideConfiguration", $scope.overrideConfiguration)
				fd.append("file", document.getElementById('import-lavagna').files[0]);
				var xhr = new XMLHttpRequest();
				xhr.open("POST", 'api/import');
				xhr.addEventListener("load", goToRootApp, false);
				xhr.addEventListener("error", goToRootApp, false);
				xhr.addEventListener("abort", goToRootApp, false);
				xhr.setRequestHeader("x-csrf-token", window.csrfToken);
				xhr.send(fd);
			};
		});

		$scope.submitBaseUrl = function () {
			//var config
			//add '/' at the end if missing
			var baseUrl = /\/$/.test($scope.baseUrl) ? $scope.baseUrl : ($scope.baseUrl + "/");

			$rootScope.toSave.first = [{first: 'BASE_APPLICATION_URL', second: baseUrl}];
			$rootScope.oauth.baseUrl = baseUrl;
			$state.go('second-step');
		}
	});

	module.controller('SetupLoginCtrl', function ($window, $rootScope, $scope, $http, $state) {


		if ($rootScope.selectedAuthMethod) {
			$scope.authMethod = $rootScope.selectedAuthMethod;
		} else if (!$scope.authMethod) {
			$scope.authMethod = 'DEMO';
		}

		$scope.checkLdapConfiguration = function (ldap, usernameAndPwd) {
			$http.post('api/check-ldap/', angular.extend({}, ldap, usernameAndPwd)).then(function (r) {
				$scope.ldapCheckResult = r.data;
			});
		}

		$scope.countSelectedOauth = function () {

			var selectedCount = 0;
			for (var k in $scope.oauth) {
				if ($scope.oauth[k].present) {
					selectedCount++;
				}
			}
			return selectedCount;
		}

		$scope.submitConfiguration = function () {
			var config = [];

			var loginType = [];


			config.push({first: 'AUTHENTICATION_METHOD', second: JSON.stringify([$scope.authMethod])});

			//ugly D:
			if ($scope.authMethod == 'LDAP') {
				loginType = ['ldap'];
				config.push({first: 'LDAP_SERVER_URL', second: $scope.ldap.serverUrl});
				config.push({first: 'LDAP_MANAGER_DN', second: $scope.ldap.managerDn});
				config.push({first: 'LDAP_MANAGER_PASSWORD', second: $scope.ldap.managerPassword});
				config.push({first: 'LDAP_USER_SEARCH_BASE', second: $scope.ldap.userSearchBase});
				config.push({first: 'LDAP_USER_SEARCH_FILTER', second: $scope.ldap.userSearchFilter});
			} else if ($scope.authMethod == 'PERSONA') {
				loginType = ['persona'];
				config.push({first: 'PERSONA_AUDIENCE', second: $scope.persona.audience});
			} else if ($scope.authMethod == 'OAUTH') {
				var newOauthConf = {baseUrl: $scope.oauth.baseUrl, providers: []};

				addProviderIfPresent(newOauthConf.providers, $scope.oauth['bitbucket'], 'bitbucket') && loginType.push('oauth.bitbucket');
				addProviderIfPresent(newOauthConf.providers, $scope.oauth['github'], 'github') && loginType.push('oauth.github');
				addProviderIfPresent(newOauthConf.providers, $scope.oauth['google'], 'google') && loginType.push('oauth.google');
				addProviderIfPresent(newOauthConf.providers, $scope.oauth['twitter'], 'twitter') && loginType.push('oauth.twitter');
				config.push({first: 'OAUTH_CONFIGURATION', second: JSON.stringify(newOauthConf)});
				$rootScope.selectedNewOauthConf = newOauthConf;
			} else if ($scope.authMethod == 'DEMO') {
				loginType = ['demo'];
			}

			$rootScope.toSave.second = config;

			$rootScope.loginType = loginType;
			$rootScope.accountProvider = loginType[0];
			$rootScope.selectedAuthMethod = $scope.authMethod;
			$state.go('third-step');
		};

	})

	module.controller('SetupUserCtrl', function ($window, $scope, $rootScope, $http, $state) {

		$scope.authMethod = $rootScope.selectedAuthMethod;

		if ($rootScope.toSave.user && $rootScope.loginType.indexOf($rootScope.toSave.user.provider) > -1) {
			$scope.accountProvider = $rootScope.toSave.user.provider;
			$scope.username = $rootScope.toSave.user.username;
		}

		$scope.saveUser = function () {
			$rootScope.toSave.user = {
				provider: $scope.accountProvider,
				username: $scope.username,
				enabled: true,
				roles: ['ADMIN']
			};
			$state.go('fourth-step');
		};
	});

	module.controller('SetupActivationCtrl', function ($window, $scope, $http, $state) {
		$scope.activate = function () {
			var configToUpdate = [{
				first: 'SETUP_COMPLETE',
				second: 'true'
			}].concat($scope.toSave.first, $scope.toSave.second);
			$http.post('api/setup/', {toUpdateOrCreate: configToUpdate, user: $scope.toSave.user}).then(goToRootApp);
		};
	})

	module.filter('mask', function ($filter) {
		return function (text) {
			if (text === undefined) {
				return null;
			}
			return text.replace(/./gi, "*");
		};
	});


})();