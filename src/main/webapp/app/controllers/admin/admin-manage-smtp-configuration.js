(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AdminManageSmtpConfigurationCtrl', function ($scope, $http, $modal, Admin) {

		var loadConfiguration = function () {
			Admin.findByKey('SMTP_ENABLED').then(function (enabled) {
				$scope.smtpEnabled = enabled.second ? JSON.parse(enabled.second) : false;
			});
			Admin.findByKey('SMTP_CONFIG').then(function (configuration) {
				$scope.configuration = configuration.second ? JSON.parse(configuration.second) : {};
			});
		};

		loadConfiguration();

		$scope.$watch('smtpEnabled', function (newVal, oldVal) {
			if (newVal == undefined || oldVal == undefined || newVal == oldVal) {
				return;
			}
			Admin.updateConfiguration(
				{
					toUpdateOrCreate: [{first: 'SMTP_ENABLED', second: newVal}]
				}
			).catch(function (error) {
					Notification.addAutoAckNotification('error', {
						key: 'notification.admin-manage-smtp-configuration.updateConfiguration.error'
					}, false);
				}).then(loadConfiguration);
		});

		$scope.saveSmtpConfig = function (conf) {
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

		$scope.openSmtpConfigModal = function () {
			$modal.open({
				templateUrl: 'partials/admin/fragments/smtp-check-modal.html',
				controller: function ($scope, $modalInstance, User) {

					User.currentCachedUser().then(function (user) {
						if (user.emailNotification) {
							$scope.to = user.email;
						}
					});

					$scope.sendTestEmail = function (conf, to) {
						return $http.post('api/check-smtp/', conf, {params: {to: to}});
					};

					$scope.close = function () {
						$modalInstance.close('done');
					}
				},
				size: 'sm',
				windowClass: 'lavagna-modal',
				resolve: {
					configuration: function () {
						return $scope.configuration;
					}
				}
			});
		}
	})
})();