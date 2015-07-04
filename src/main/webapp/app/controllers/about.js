(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AboutLicenseCtrl', function ($scope, $http) {
		$http.get('about/LICENSE-GPLv3.txt').success(function(res) {
			$scope.license=res;
		});
	});
	
	module.controller('AboutThirdPartyCtrl', function ($scope, $http) {
		$http.get('about/THIRD-PARTY.txt').success(function(res) {
			$scope.thirdParty=res;
		});
	});
	
})();