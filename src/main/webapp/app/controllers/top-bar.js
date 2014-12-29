(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('TopBarCtrl', function ($scope, $window, User) {
		User.current().then(function (u) {
			$scope.user = u;
		});

		
		$scope.login = function () {
			var reqUrlWithoutContextPath = $window.location.pathname.substr($("base").attr('href').length - 1);
			$window.location.href = 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
		};

		
	});
})();