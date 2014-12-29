(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AdminParametersCtrl', function ($scope, Admin, Notification) {

		
		var loadAll = function() {
			$scope.configurable = {};
			var configurableKeys = ['TRELLO_API_KEY', 'MAX_UPLOAD_FILE_SIZE', 'USE_HTTPS'];
			angular.forEach(configurableKeys, function(v) {
				Admin.findByKey(v).then(function(res) {
					$scope.configurable[res.first] = res.second;
				});
			});
			
			Admin.findAllConfiguration().then(function (res) {
				$scope.conf = res;
			});
		};
		
		loadAll();
		
		$scope.updateConfiguration = function(k,v) {
			Admin.updateKeyConfiguration(k,v).then(function() {
				Notification.addAutoAckNotification('success', {
					key: 'notification.admin-parameters.updateConfiguration.success',
					parameters: { parameterName : k }
				}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', {
					key: 'notification.admin-parameters.updateConfiguration.error',
					parameters: { parameterName : k }
				}, false);
			}).then(loadAll)
		};
		
		$scope.deleteConfiguration = function(k) {
			Admin.deleteKeyConfiguration(k).then(function() {
				Notification.addAutoAckNotification('success', {
					key: 'notification.admin-parameters.deleteConfiguration.success',
					parameters: { parameterName : k }
				}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', {
					key: 'notification.admin-parameters.deleteConfiguration.error',
					parameters: { parameterName : k }
				}, false);
			}).then(loadAll)
		};
		
	});
})();