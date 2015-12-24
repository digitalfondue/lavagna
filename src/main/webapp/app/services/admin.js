(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	var extractData = function (data) {
		return data.data
	};

	services.factory('Admin', function ($http, $upload) {
		return {
			endpointInfo: function () {
				return $http.get('api/admin/endpoint-info').then(extractData);
			},

			findAllConfiguration: function () {
				return $http.get('api/application-configuration/').then(extractData);
			},

			findByKey: function (key) {
				return $http.get('api/application-configuration/' + key).then(extractData);
			},
			updateConfiguration: function (values) {
				return $http.post('api/application-configuration/', values).then(extractData);
			},
			updateKeyConfiguration: function(k,v) {
				return $http.post('api/application-configuration/', {toUpdateOrCreate:[{first:k, second:v}]}).then(extractData);
			},
			deleteKeyConfiguration: function(key) {
				return $http['delete']('api/application-configuration/' + key).then(extractData);
			},
			checkHttpsConfiguration: function () {
				return $http.get('api/check-https-config').then(extractData);
			},

			checkLdap: function (ldap, usernameAndPwd) {
				return $http.post('api/check-ldap/', angular.extend({}, ldap, usernameAndPwd)).then(extractData);
			},

			importUsers: function(file, progressCallBack, completeCallback, failedCallback, canceledCallback) {
				var upload = $upload.upload({
			        url: 'api/user/bulk-insert',
			        file: file
			      }).progress(progressCallBack)
			      .success(completeCallback)
			      .error(failedCallback)
			      .xhr(function(xhr){xhr.addEventListener("abort", canceledCallback, false);});
				return upload;
		    },

			importData: function(file, overrideConfig, progressCallBack, completeCallback, failedCallback) {
				var upload = $upload.upload({
			        url: 'api/import/lavagna',
			        file: file,
			        data: {overrideConfiguration: overrideConfig}
				}).progress(progressCallBack)
				.success(completeCallback)
				.error(failedCallback);

				return upload;
			},

			//----
			findAllLoginHandlers : function() {
				return $http.get('api/login/all').then(extractData);
			},
			findAllOauthProvidersInfo : function() {
				return $http.get('api/login/oauth/all').then(extractData);
			},
			findAllBaseLoginWithActivationStatus : function() {
				return $http.get('api/login/all-base-with-activation-status').then(extractData);
			},
			testSmtpConfig: function(to) {
                return $http.post('api/check-smtp/', configuration, {params: {to: to}});
			}
		};
	});
})();
