(function () {
    'use strict';

    var module = angular.module('lavagnaLogin', ['ngSanitize', 'ngMessages', 'ngMaterial', 'pascalprecht.translate']);

    module.value('configuration', JSON.parse(document.getElementById('app-configuration').textContent));

    function hasErrorInQueryString($window, error) {
        return $window.location.search.indexOf('?' + error) !== -1 || $window.location.search.indexOf('&' + error) !== -1;
    }

    module.config(['$mdThemingProvider', '$compileProvider', '$translateProvider', function ($mdThemingProvider, $compileProvider, $translateProvider) {
        $compileProvider.debugInfoEnabled(false);
        $compileProvider.preAssignBindingsEnabled(true);
        $mdThemingProvider.disableTheming();

        var locale = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage);

        angular.forEach(io_lavagna.i18n, function (map, lang) {
            $translateProvider.translations(lang, map);
        });
        $translateProvider.preferredLanguage(locale);
        $translateProvider.fallbackLanguage('en');
        $translateProvider.usePostCompiling(true);
        $translateProvider.useSanitizeValueStrategy('escape');
    }]);

    // -----------------
    module.component('demoLogin', {
        template: ['<div ng-if="$ctrl.show" class="lvg-panel">',
            '<form method="post" action="login/demo/" class="lvg-panel__body">',
            '<md-input-container class="md-block">',
            '<label for="demo-username">{{\'login.demo.username\' | translate}}</label>',
            '<input id="demo-username" type="text" placeholder="User name" name="username" autocomplete="off" required>',
            '</md-input-container>',
            '<md-input-container class="md-block">',
            '<label for="demo-password">{{\'login.demo.password\' | translate}}</label>',
            '<input id="demo-password" type="password" placeholder="Password" name="password" autocomplete="off" required>',
            '</md-input-container>',
            '<div ng-if="$ctrl.errorDemo" class="error-message">{{\'login.error\' | translate}}</div>',
            '<p>',
            '<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> {{\'login.rememberMe\' | translate}}</md-checkbox>',
            '</p>',
            '<p class="single-button">',
            '<md-button type="submit" class="md-primary">Log in</md-button>',
            '<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}"> ',
            '<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}">',
            '<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" />',
            '</p>',
            '</form>',
            '</div>'].join(''),
        controller: ['configuration', '$window', DemoLoginCtrl]
    });

    function DemoLoginCtrl(configuration, $window) {
        var ctrl = this;

        ctrl.configuration = configuration;
        ctrl.show = configuration.loginDemo === 'block';
        ctrl.errorDemo = hasErrorInQueryString($window, 'error-demo');
    }
    // -----------------

    module.component('passwordLogin', {
        template: ['<div ng-if="$ctrl.show" class="lvg-panel">',
            '<form method="post" action="login/password/" class="lvg-panel__body">',
            '<md-input-container class="md-block">',
            '<label for="demo-username">{{\'login.password.username\' | translate}}</label>',
            '<input id="demo-username" type="text" placeholder="User name" name="username" autocomplete="off" required>',
            '</md-input-container>',
            '<md-input-container class="md-block">',
            '<label for="demo-password">{{\'login.password.password\' | translate}}</label>',
            '<input id="demo-password" type="password" placeholder="Password" name="password" autocomplete="off" required>',
            '</md-input-container>',
            '<div ng-if="$ctrl.errorDemo" class="error-message">{{\'login.error\' | translate}}</div>',
            '<p>',
            '<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> {{\'login.rememberMe\' | translate}}</md-checkbox>',
            '</p>',
            '<p class="single-button">',
            '<md-button type="submit" class="md-primary">Log in</md-button>',
            '<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}"> ',
            '<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}">',
            '<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" />',
            '</p>',
            '</form>',
            '</div>'].join(''),
        controller: ['configuration', '$window', PasswordLoginCtrl]
    });

    function PasswordLoginCtrl(configuration, $window) {
        var ctrl = this;

        ctrl.configuration = configuration;
        ctrl.show = configuration.loginPassword === 'block';
        ctrl.errorDemo = hasErrorInQueryString($window, 'error-password');
    }
    // -----------------

    module.component('ldapLogin', {
        template: ['<div ng-if="$ctrl.show" class="lvg-panel">',
            '<form method="post" action="login/ldap/" class="lvg-panel__body">',
            '<md-input-container class="md-block">',
            '<label for="ldap-username">Username</label>',
            '<input id="ldap-username" type="text" placeholder="User name" name="username" autocomplete="off" required>',
            '</md-input-container>',
            '<md-input-container class="md-block">',
            '<label for="ldap-password">Password</label>',
            '<input id="ldap-password" type="password" placeholder="Password" name="password" autocomplete="off" required>',
            '</md-input-container>',
            '<div ng-if="$ctrl.errorLdap" class="error-message">{{\'login.error\' | translate}}</div>',
            '<p>',
            '<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> {{\'login.rememberMe\' | translate}}</md-checkbox>',
            '</p>',
            '<p class="single-button">',
            '<md-button type="submit" class="md-primary">Log in</md-button>',
            '<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}"> ',
            '<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}">',
            '<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" />',
            '</p>',
            '</form>',
            '</div>'].join(''),
        controller: ['configuration', '$window', LdapLoginCtrl]
    });

    function LdapLoginCtrl(configuration, $window) {
        var ctrl = this;

        ctrl.configuration = configuration;
        ctrl.show = configuration.loginLdap === 'block';
        ctrl.errorLdap = hasErrorInQueryString($window, 'error-ldap');
    }

    module.component('oauthLogin', {
        template: ['<div ng-if="$ctrl.show" class="lvg-panel">',
            '<div class="lvg-panel__body">',
            '<div ng-if="$ctrl.errorOauth" class="error-message">{{\'login.error\' | translate}}</div>',
            '<div class="single-button" ng-repeat="provider in $ctrl.configuration.loginOauthProviders">',
            '<form method="post" action="{{::$ctrl.url(provider)}}">',
            '<md-button type="submit" class="btn-oauth btn-{{::provider}}">Login with {{::provider}}</md-button>',
            '<input type="hidden" name="_csrf" value="{{::$ctrl.configuration.csrfToken}}" />',
            '<input type="hidden" name="rememberMe" value="{{$ctrl.rememberMe}}" />',
            '<input type="hidden" name="reqUrl" value="{{::$ctrl.configuration.reqUrl}}" />',
            '</form>',
            '</div>',

            '<p  class="single-button">',
            '<md-checkbox ng-model="$ctrl.rememberMe" ng-false-value="null"> {{\'login.rememberMe\' | translate}}</md-checkbox>',
            '</p></div></div>'].join(''),
        controller: ['configuration', '$window', OauthLoginCtrl]
    });

    function OauthLoginCtrl(configuration, $window) {
        var ctrl = this;

        ctrl.configuration = configuration;
        ctrl.show = configuration.loginOauth === 'block';
        ctrl.url = function url(provider) {
            return 'login/oauth/' + provider;
        };
        ctrl.errorOauth = hasErrorInQueryString($window, 'error-oauth');
    }
}());
