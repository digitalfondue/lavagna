(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminLogin', {
        bindings: {
            oauthProviders: '='
        },
        templateUrl: 'app/components/admin/login/login.html',
        controller: ['$mdDialog', '$q', 'Admin', 'Permission', 'Notification', 'User', 'CONTEXT_PATH', AdminLoginController],
    });

    function AdminLoginController($mdDialog, $q, Admin, Permission, Notification, User, CONTEXT_PATH) {
        var ctrl = this;

        ctrl.oauthNewProvider = {};

        function loadConfiguration() {
            return $q.all([Admin.findAllConfiguration(), Admin.findAllBaseLoginWithActivationStatus(), Admin.findAllOauthProvidersInfo()]).then(function (res) {
                var conf = res[0];
                var allBaseLogin = res[1];

                ctrl.oauthProviders = res[2];

                ctrl.currentConf = conf;
                ctrl.authMethod = allBaseLogin;

                ctrl.ldap = {
                    serverUrl: conf['LDAP_SERVER_URL'],
                    managerDn: conf['LDAP_MANAGER_DN'],
                    managerPassword: conf['LDAP_MANAGER_PASSWORD'],
                    userSearchBase: conf['LDAP_USER_SEARCH_BASE'],
                    userSearchFilter: conf['LDAP_USER_SEARCH_FILTER'],
                    autoCreateMissingAccount: conf['LDAP_AUTOCREATE_MISSING_ACCOUNT'] === 'true'
                };

                var oauth = conf['OAUTH_CONFIGURATION'] && JSON.parse(conf['OAUTH_CONFIGURATION']) || { providers: []};

                ctrl.oauth = {};

                angular.forEach(ctrl.oauthProviders, function (p) {
                    ctrl.oauth[p.name] = {};
                });

                ctrl.oauth.baseUrl = oauth.baseUrl || CONTEXT_PATH;

                for (var i = 0; i < oauth.providers.length;i++) {
                    ctrl.oauth[oauth.providers[i].provider] = oauth.providers[i];
                    ctrl.oauth[oauth.providers[i].provider].present = true;
                }
            });
        }

        loadConfiguration();

        // -- enable providers

        var updateActiveProviders = function () {
            var toUpdate = [];

            var activeAuthMethod = [];

            for (var k in ctrl.authMethod) {
                if (ctrl.authMethod.hasOwnProperty(k) && ctrl.authMethod[k]) {
                    activeAuthMethod.push(k);
                }
            }

            toUpdate.push({first: 'AUTHENTICATION_METHOD', second: JSON.stringify(activeAuthMethod)});

            Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).catch(function () {
                Notification.addAutoAckNotification('error', { key: 'notification.admin-configure-login.updateActiveProviders.error'}, false);
            }).then(loadConfiguration);
        };

        ctrl.updateActiveProviders = function (value, identifier) {
            ctrl.authMethod[identifier] = value;
            updateActiveProviders();
        };

        // -- save providers config

        ctrl.saveLdapConfig = function (ldap) {
            var toUpdate = [];

            toUpdate.push({first: 'LDAP_SERVER_URL', second: ldap.serverUrl});
            toUpdate.push({first: 'LDAP_MANAGER_DN', second: ldap.managerDn});
            toUpdate.push({first: 'LDAP_MANAGER_PASSWORD', second: ldap.managerPassword});
            toUpdate.push({first: 'LDAP_USER_SEARCH_BASE', second: ldap.userSearchBase});
            toUpdate.push({first: 'LDAP_USER_SEARCH_FILTER', second: ldap.userSearchFilter});
            toUpdate.push({first: 'LDAP_AUTOCREATE_MISSING_ACCOUNT', second: ldap.autoCreateMissingAccount || false});

            Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).then(function () {
                Notification.addAutoAckNotification('success', { key: 'notification.admin-configure-login.saveLdapConfig.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', { key: 'notification.admin-configure-login.saveLdapConfig.error'}, false);
            }).then(loadConfiguration);
        };

        function addProviderIfPresent(list, conf, provider) {
            if (conf && conf.present) {
                list.push({provider: provider, apiKey: conf.apiKey, apiSecret: conf.apiSecret,
                    hasCustomBaseAndProfileUrl: conf.hasCustomBaseAndProfileUrl,
                    baseProvider: conf.baseProvider,
                    baseUrl: conf.baseUrl,
                    profileUrl: conf.profileUrl,
                    autoCreateMissingAccount: conf.autoCreateMissingAccount});
            }
        }

        ctrl.saveOauthConfig = function saveOauthConfig(oauthNewProvider) {
            var toUpdate = [];

            var newOauthConf = {baseUrl: ctrl.oauth.baseUrl, providers: []};

            angular.forEach(ctrl.oauthProviders, function (provider) {
                addProviderIfPresent(newOauthConf.providers, ctrl.oauth[provider.name], provider.name);
            });

            if (oauthNewProvider) {
                newOauthConf.providers.push({
                    provider: oauthNewProvider.type.name + '-' + oauthNewProvider.name,
                    apiKey: oauthNewProvider.apiKey,
                    apiSecret: oauthNewProvider.apiSecret,
                    hasCustomBaseAndProfileUrl: true,
                    baseProvider: oauthNewProvider.type.name,
                    baseUrl: oauthNewProvider.baseUrl,
                    profileUrl: oauthNewProvider.profileUrl,
                    autoCreateMissingAccount: oauthNewProvider.autoCreateMissingAccount
                });
            }

            toUpdate.push({first: 'OAUTH_CONFIGURATION', second: JSON.stringify(newOauthConf)});

            return Admin.updateConfiguration({toUpdateOrCreate: toUpdate}).then(function () {
                Notification.addAutoAckNotification('success', { key: 'notification.admin-configure-login.saveOAuthConfig.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', { key: 'notification.admin-configure-login.saveOAuthConfig.error'}, false);
            }).then(loadConfiguration);
        };

        // TODO: in the future, move to a separate component
        ctrl.openOauthAddNewModal = function () {
            $mdDialog.show({
                templateUrl: 'app/components/admin/login/oauth/oauth-modal.html',
                controller: ['$scope', 'oauth', 'oauthProviders',
                    function ($scope, oauth, oauthProviders) {
                        $scope.oauth = oauth;
                        $scope.oauthProviders = oauthProviders;

                        $scope.saveOauthConfig = function (toSave) {
                            ctrl.saveOauthConfig(toSave).then(function () {
                                $mdDialog.hide();
                            });
                        };

                        $scope.close = function () {
                            $mdDialog.hide();
                        };
                    }],
                resolve: {
                    oauth: function () {
                        return ctrl.oauth;
                    },
                    oauthProviders: function () {
                        return ctrl.oauthProviders;
                    }
                }
            });
        };

        // TODO: in the future, move to a separate component
        ctrl.openLdapConfigModal = function () {
            function LdapModalCtrl(ldapConfig) {
                var ctrl = this;

                ctrl.checkLdapConfiguration = function (ldapCheck) {
                    Admin.checkLdap(ldapConfig, ldapCheck).then(function (r) {
                        ctrl.ldapCheckResult = r;
                    });
                };

                ctrl.close = function () {
                    $mdDialog.hide();
                };
            }

            $mdDialog.show({
                templateUrl: 'app/components/admin/login/ldap/ldap-modal.html',
                controllerAs: '$ctrl',
                bindToController: true,
                controller: ['ldapConfig', LdapModalCtrl],
                resolve: {
                    ldapConfig: function () {
                        return ctrl.ldap;
                    }
                }
            });
        };

        // ------ Anonymous access

        var load = function () {
            Admin.findByKey('ENABLE_ANON_USER').then(function (res) {
                ctrl.anonEnabled = res.second === 'true';
            });

            Permission.findUsersWithRole('ANONYMOUS').then(function (res) {
                var userHasGlobalRole = false;

                for (var i = 0; i < res.length; i++) {
                    if (res[i].provider === 'system' && res[i].username === 'anonymous') {
                        userHasGlobalRole = true;
                    }
                }
                ctrl.userHasGlobalRole = userHasGlobalRole;
            });

            Permission.findAllRolesAndRelatedPermissions().then(function (res) {
                var userHasSearchPermission = false;

                for (var i = 0; i < res['ANONYMOUS'].roleAndPermissions.length; i++) {
                    var permission = res['ANONYMOUS'].roleAndPermissions[i];

                    if (permission.permission === 'SEARCH') {
                        userHasSearchPermission = true;
                    }
                }
                ctrl.userHasSearchPermission = userHasSearchPermission;
            });
        };

        load();

        ctrl.anonChange = function (value) {
            if (!angular.isDefined(value)) {
                return;
            }

            Admin.updateConfiguration(
                {toUpdateOrCreate:
                        [{first: 'ENABLE_ANON_USER', second: value.toString()}]}
                ).catch(function () {
                    Notification.addAutoAckNotification('error', { key: 'notification.admin-configure-login.updateAnonConfiguration.error'}, false);
                }).then(load);
        };

        ctrl.globalRoleChange = function (value) {
            if (!angular.isDefined(value)) {
                return;
            }
            User.byProviderAndUsername('system', 'anonymous').then(function (res) {
                if (value) {
                    Permission.addUserToRole(res.id, 'ANONYMOUS').then(load);
                } else {
                    Permission.removeUserToRole(res.id, 'ANONYMOUS').then(load);
                }
            });
        };

        ctrl.searchRoleChange = function (value) {
            if (!angular.isDefined(value)) {
                return;
            }

            Permission.toggleSearchPermissionForAnonymousUsers(value).then(load);
        };
    }
}());
