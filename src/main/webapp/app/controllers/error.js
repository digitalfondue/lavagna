(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ErrorCtrl', function ($stateParams, $scope) {
		$scope.parentResource = {
			url: decodeURIComponent($stateParams.resourceId),
			show: ($stateParams.resourceId !== '')
		};
	});
})();