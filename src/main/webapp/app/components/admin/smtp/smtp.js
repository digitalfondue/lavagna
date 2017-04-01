(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminSmtp', {
        templateUrl: 'app/components/admin/smtp/smtp.html',
        controller: ['$mdDialog', 'Admin', 'Notification', AdminSmtpController]
    });

    function AdminSmtpController($mdDialog, Admin, Notification) {
        var ctrl = this;

        ctrl.toggleSmtp = toggleSmtp;
        ctrl.saveSmtpConfig = saveSmtpConfig;
        ctrl.openSmtpConfigModal = openSmtpConfigModal;

        ctrl.$onInit = function init() {
            loadConfiguration();
        };

        function loadConfiguration() {
            Admin.findByKey('SMTP_ENABLED').then(function (enabled) {
                ctrl.smtpEnabled = enabled.second ? JSON.parse(enabled.second) : false;
            });
            Admin.findByKey('SMTP_CONFIG').then(function (configuration) {
                ctrl.configuration = configuration.second ? JSON.parse(configuration.second) : {};
            });
        }

        function toggleSmtp(value) {
            ctrl.smtpEnabled = value;
            Admin.updateConfiguration(
                {
                    toUpdateOrCreate: [{first: 'SMTP_ENABLED', second: value}]
                }
            ).catch(function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-manage-smtp-configuration.updateConfiguration.error'
                }, false);
            }).then(loadConfiguration);
        }

        function saveSmtpConfig(conf) {
            Admin.updateConfiguration({
                toUpdateOrCreate: [
                    {first: 'SMTP_CONFIG', second: JSON.stringify(conf)}
                ]
            }).then(function () {
                Notification.addAutoAckNotification('success', {
                    key: 'notification.admin-manage-smtp-configuration.saveSmtpConfig.success'
                }, false);
            }, function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-manage-smtp-configuration.saveSmtpConfig.error'
                }, false);
            }).then(loadConfiguration);
        }

        function openSmtpConfigModal() {
            $mdDialog.show({
                templateUrl: 'app/components/admin/smtp/smtp-modal.html',
                controller: function ($scope, configuration, User, Notification) {
                    User.currentCachedUser().then(function (user) {
                        if (user.emailNotification) {
                            $scope.to = user.email;
                        }
                    });

                    $scope.sendTestEmail = function (to) {
                        Notification.addAutoAckNotification('success', {key: 'notification.smtp-configuration.sending'}, false);
                        Admin.testSmtpConfig(configuration, to)
                            .then(function () {
                                Notification.addAutoAckNotification('success', {key: 'notification.smtp-configuration.success'}, false);
                                $mdDialog.hide();
                            }).catch(function () {
                                Notification.addAutoAckNotification('error', {key: 'notification.smtp-configuration.error'}, false);
                            });
                    };

                    $scope.close = function () {
                        $mdDialog.hide();
                    };
                },
                resolve: {
                    configuration: function () {
                        return ctrl.configuration;
                    }
                }
            });
        }
    }
}());
