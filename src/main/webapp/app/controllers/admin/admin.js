(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AdminCtrl', function ($state, $scope, $rootScope, Admin) {

		Admin.checkHttpsConfiguration().then(function (res) {
			$scope.httpsConfigurationCheck = res;
		});
		
	});
})();