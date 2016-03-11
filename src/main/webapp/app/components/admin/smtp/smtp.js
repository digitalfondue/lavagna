(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentAdminSmtp', {
        bindings: {},
        controller: AdminSmtpController,
        controllerAs: 'adminSmtpCtrl',
        templateUrl: 'app/components/admin/smtp/smtp.html'
    })

    function AdminSmtpController($mdDialog, Admin, Notification) {
        var ctrl = this;

        var loadConfiguration = function () {
			Admin.findByKey('SMTP_ENABLED').then(function (enabled) {
				ctrl.smtpEnabled = enabled.second ? JSON.parse(enabled.second) : false;
			});
			Admin.findByKey('SMTP_CONFIG').then(function (configuration) {
				ctrl.configuration = configuration.second ? JSON.parse(configuration.second) : {};
			});
		};

		loadConfiguration();

		ctrl.toggleSmtp = function(value) {
		    ctrl.smtpEnabled = value;
			Admin.updateConfiguration(
                {
                    toUpdateOrCreate: [{first: 'SMTP_ENABLED', second: value}]
                }
			).catch(function (error) {
					Notification.addAutoAckNotification('error', {
						key: 'notification.admin-manage-smtp-configuration.updateConfiguration.error'
					}, false);
				}).then(loadConfiguration);
		};

		ctrl.saveSmtpConfig = function (conf) {
			Admin.updateConfiguration({
				toUpdateOrCreate: [
					{first: 'SMTP_CONFIG', second: JSON.stringify(conf)}
				]
			}).then(function () {
				Notification.addAutoAckNotification('success', {
					key: 'notification.admin-manage-smtp-configuration.saveSmtpConfig.success'
				}, false);
			}, function (error) {
				Notification.addAutoAckNotification('error', {
					key: 'notification.admin-manage-smtp-configuration.saveSmtpConfig.error'
				}, false);
			}).then(loadConfiguration);
		};

		ctrl.openSmtpConfigModal = function () {
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
						Admin.testSmtpConfig(to)
							.success(function () {
								Notification.addAutoAckNotification('success', {key: 'notification.smtp-configuration.success'}, false);
							}).error(function () {
								Notification.addAutoAckNotification('error', {key: 'notification.smtp-configuration.error'}, false);
							});
					};

					$scope.close = function () {
						$mdDialog.hide();
					}
				},
				resolve: {
					configuration: function () {
						return ctrl.configuration;
					}
				}
			});
		}

    };

})();
