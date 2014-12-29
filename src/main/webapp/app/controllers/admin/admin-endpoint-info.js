(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AdminEndpointInfoCtrl', function ($scope, Admin) {
		Admin.endpointInfo().then(function (res) {
			$scope.endpointInfo = res;
		});
	});
})();