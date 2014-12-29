(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectManageCtrl', function($scope, project) {

		$scope.sidebarOpen = true;
		$scope.project = project;
	});
})();