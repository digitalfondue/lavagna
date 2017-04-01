(function () {
    var module = angular.module('lavagna-setup');

    module.component('setupSecondStep', {
        controller: ['$window', 'Configuration', '$http', '$state', SetupLoginCtrl],
        templateUrl: 'components/second-step/second-step.html'
    });

    function SetupLoginCtrl($window, Configuration, $http, $state) {
        var ctrl = this;

        ctrl.ldap = Configuration.secondStep.ldap;
        ctrl.oauth = Configuration.secondStep.oauth;
        ctrl.oauth.baseUrl = Configuration.toSave.first[0].second;

        ctrl.oauthProviders = Configuration.secondStep.oauthProviders;
        ctrl.oauthCustomizable = Configuration.secondStep.oauthCustomizable;
        ctrl.oauthNewProvider = Configuration.secondStep.oauthNewProvider;

        if (Configuration.selectedAuthMethod) {
            ctrl.authMethod = Configuration.selectedAuthMethod;
        } else if (!ctrl.authMethod) {
            ctrl.authMethod = 'PASSWORD';
        }

        ctrl.checkLdapConfiguration = function (ldap, usernameAndPwd) {
            $http.post('api/check-ldap/', angular.extend({}, ldap, usernameAndPwd)).then(function (r) {
                ctrl.ldapCheckResult = r.data;
            });
        };

        ctrl.countSelectedOauth = function () {
            var selectedCount = 0;

            for (var k in ctrl.oauth) {
                if (ctrl.oauth[k].present) {
                    selectedCount++;
                }
            }

            return selectedCount;
        };

        ctrl.back = function () {
            $state.go('first-step');
        };

        ctrl.submitConfiguration = function () {
            var config = [];

            var loginType = [];

            config.push({first: 'AUTHENTICATION_METHOD', second: JSON.stringify([ctrl.authMethod])});

            // ugly D:
            if (ctrl.authMethod === 'LDAP') {
                loginType = ['ldap'];
                config.push({first: 'LDAP_SERVER_URL', second: ctrl.ldap.serverUrl});
                config.push({first: 'LDAP_MANAGER_DN', second: ctrl.ldap.managerDn});
                config.push({first: 'LDAP_MANAGER_PASSWORD', second: ctrl.ldap.managerPassword});
                config.push({first: 'LDAP_USER_SEARCH_BASE', second: ctrl.ldap.userSearchBase});
                config.push({first: 'LDAP_USER_SEARCH_FILTER', second: ctrl.ldap.userSearchFilter});
            } else if (ctrl.authMethod === 'OAUTH') {
                var newOauthConf = {baseUrl: ctrl.oauth.baseUrl, providers: []};

                if (addProviderIfPresent(newOauthConf.providers, ctrl.oauth['bitbucket'], 'bitbucket')) {
                    loginType.push('oauth.bitbucket');
                }

                if (addProviderIfPresent(newOauthConf.providers, ctrl.oauth['gitlab'], 'gitlab')) {
                    loginType.push('oauth.gitlab');
                }

                if (addProviderIfPresent(newOauthConf.providers, ctrl.oauth['github'], 'github')) {
                    loginType.push('oauth.github');
                }

                if (addProviderIfPresent(newOauthConf.providers, ctrl.oauth['google'], 'google')) {
                    loginType.push('oauth.google');
                }

                if (addProviderIfPresent(newOauthConf.providers, ctrl.oauth['twitter'], 'twitter')) {
                    loginType.push('oauth.twitter');
                }

                if (ctrl.oauthNewProvider.type) {
                    var providerName = ctrl.oauthNewProvider.type + '-' + ctrl.oauthNewProvider.name;

                    newOauthConf.providers.push({
                        provider: providerName,
                        apiKey: ctrl.oauthNewProvider.apiKey,
                        apiSecret: ctrl.oauthNewProvider.apiSecret,
                        hasCustomBaseAndProfileUrl: true,
                        baseProvider: ctrl.oauthNewProvider.type,
                        baseUrl: ctrl.oauthNewProvider.baseUrl
                    });

                    loginType.push('oauth.' + providerName);
                }

                config.push({first: 'OAUTH_CONFIGURATION', second: JSON.stringify(newOauthConf)});
                Configuration.selectedNewOauthConf = newOauthConf;
            } else if (ctrl.authMethod === 'DEMO') {
                loginType = ['demo'];
            } else if (ctrl.authMethod === 'PASSWORD') {
                loginType = ['password'];
            }

            Configuration.toSave.second = config;

            Configuration.loginType = loginType;
            Configuration.accountProvider = loginType[0];
            Configuration.selectedAuthMethod = ctrl.authMethod;
            $state.go('third-step');
        };
    }

    function addProviderIfPresent(list, conf, provider) {
        if (conf && conf.present) {
            list.push({provider: provider, apiKey: conf.apiKey, apiSecret: conf.apiSecret});

            return true;
        }

        return false;
    }
}());
