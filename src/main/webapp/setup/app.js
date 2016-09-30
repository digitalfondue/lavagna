(function () {
	'use strict';

	function getOrigin(window) {
		if (!window.location.origin) {
			window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
		}
		return window.location.origin;
	}

	function getPort(window) {
		return window.location.port || (window.location.protocol === "https:" ? "443" : "80")
	}

	var module = angular.module('lavagna-setup', ['ui.router', 'ngSanitize', 'ngMessages', 'ngMaterial']);

	module.config(function ($stateProvider, $urlRouterProvider, $httpProvider) {
		$stateProvider.state('first-step', {
			url: '/',
			template: '<setup-first-step></setup-first-step>',
		}).state('second-step', {
			url: '/login',
			template: '<setup-second-step></setup-second-step>',
		}).state('third-step', {
			url: '/user',
			template: '<setup-third-step></setup-third-step>',
		}).state('fourth-step', {
			url: '/confirmation',
			template: '<setup-fourth-step></setup-fourth-step>'
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

		/*$rootScope.ldap = {};*/
		/*$rootScope.oauth = {baseUrl: getOrigin($window) + $window.location.pathname.replace(/setup\/$/, '')};*/
		/*$rootScope.oauthProviders = ['bitbucket', 'gitlab', 'github', 'google', 'twitter'];
		$rootScope.oauthCustomizable = ['gitlab'];*/
		/*$rootScope.oauthNewProvider = {};
		angular.forEach($rootScope.oauthProviders, function (p) {
			$rootScope.oauth[p] = {present: false};
		});*/

		$rootScope.toSave = {first: undefined, second: undefined, user: undefined};


		//redirect to first page if the user is bypassing it (as we are using the rootscope for propagating the values)
		if (!$rootScope.fromFirstStep) {
			$state.go('first-step');
		}
	});

	function goToRootApp() {
		window.location.href = getOrigin(window) + window.location.pathname.replace(/setup\/$/, '');
	}
	
	

	module.filter('mask', function ($filter) {
		return function (text) {
			if (text === undefined) {
				return null;
			}
			return text.replace(/./gi, "*");
		};
	});


})();
