(function() {

	'use strict';
	
	
	var module = angular.module('lavagnaLogin', ['ngSanitize', 'ngMessages', 'ngMaterial']);
	module.value('configuration', JSON.parse(document.getElementById('app-configuration').textContent))
	
	
	//-----------------
	module.component('demoLogin', {
		template: ['<div class="login-demo" ng-if="$ctrl.show">',
			'<div class="error-message-demo">Username/Password is not correct or the account is not enabled</div>',
			'<form method="post" action="login/demo/">',
			  '<md-input-container class="md-block">',
                   '<label for="demo-username">Demo Username</label>',
                   '<input id="demo-username" type="text" placeholder="User name" name="username" autocomplete="off" class="form-control" required>',
			  '</md-input-container>',
			  '<md-input-container class="md-block">',
                '<label for="demo-password">Demo Password </label>',
                '<input id="demo-password" type="password" placeholder="Password" name="password" autocomplete="off" class="form-control" required>',
			  '</md-input-container>',
				'<p class="single-button">',
					'<button type="submit" class="lvg-button lvg-button-submit">Log in</button>',
					'<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}"> ',
					'<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}">',
					'<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" class="remember-me" />',
				'</p>',
				'<p class="single-button remember-me">',
					'<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> remember me</md-checkbox>',
				'</p>',
			'</form>',
		'</div>'].join(''),
		controller: ['configuration', demoLoginCtrl]
	});
	
	
	function demoLoginCtrl(configuration) {
		var ctrl = this;
		ctrl.configuration = configuration;
		ctrl.show = configuration.loginDemo === 'block';
	}
	//-----------------
	
	module.component('ldapLogin', {
        template: ['<div class="login-ldap" ng-if="$ctrl.show">',
		           		'<form method="post" action="login/ldap/">',
		           			'<div class="error-message-ldap">Username/Password is not correct or the account is not enabled</div>',
		           			'<md-input-container class="md-block">',
		           				'<label for="ldap-username">Username</label>',
		           				'<input id="ldap-username" type="text" placeholder="User name" name="username" autocomplete="off" class="form-control" required>',
		           			'</md-input-container>',
		           			'<md-input-container class="md-block">',
		           				'<label for="ldap-password">Password</label>',
		           				'<input id="ldap-password" type="password" placeholder="Password" name="password" autocomplete="off" class="form-control" required>',
		           			'</md-input-container>',
		           			'<p class="single-button">',
		           				'<button type="submit" class="lvg-button lvg-button-submit">Log in</button>',
		           				'<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}"> ',
		           				'<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}">',
		           				'<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" class="remember-me" />',
		           			'</p>',
		           			'<p class="single-button remember-me">',
		           				'<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> remember me</md-checkbox>',
		           			'</p>',
		           		'</form>',
		           	'</div>'].join(''),
		controller: ['configuration', ldapLoginCtrl] 
	});
	
	function ldapLoginCtrl(configuration) {
		var ctrl = this;
		ctrl.configuration = configuration;
		ctrl.show = configuration.loginLdap === 'block';
	}
	
	
	module.component('oauthLogin', {
		template: ['<div class="login-oauth" ng-if="$ctrl.show">',
		'<div class="error-message-oauth">Username/Password is not correct or the account is not enabled</div>',
		'<div class="single-button" ng-repeat="provider in $ctrl.configuration.loginOauthProviders">',
			'<form method="post" action="{{::$ctrl.url(provider)}}">',
				'<button type="submit" class="lvg-button btn-oauth btn-{{::provider}}">Login with {{::provider}}</button>',
				'<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}" />', 
				'<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" class="remember-me" />',
				'<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}" />',
			'</form>',
		'</div>',
	
		'<p  class="single-button remember-me">',
			'<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> remember me</md-checkbox>',
		'</p>'].join(''),
		controller: ['configuration', oauthLoginCtrl]
	});
	
	function oauthLoginCtrl(configuration) {
		var ctrl = this;
		ctrl.configuration = configuration;
		ctrl.show = configuration.loginOauth === 'block';
		ctrl.url = function url(provider) {
			return 'login/oauth/'+provider;
		}
		
	}
	
})();