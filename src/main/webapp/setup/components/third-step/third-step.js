(function() {
	
	var module = angular.module('lavagna-setup');
	
	module.component('setupThirdStep', {
		controller: SetupUserCtrl,
		templateUrl: 'components/third-step/third-step.html'
	});
	
	
	function SetupUserCtrl($window, $rootScope, $http, $state) {
		
		var ctrl = this;

		ctrl.authMethod = $rootScope.selectedAuthMethod;
		ctrl.loginType = $rootScope.loginType;
		

		if ($rootScope.toSave.user && $rootScope.loginType.indexOf($rootScope.toSave.user.provider) > -1) {
			ctrl.accountProvider = $rootScope.toSave.user.provider;
			ctrl.username = $rootScope.toSave.user.username;
		}

		ctrl.saveUser = function () {
			$rootScope.toSave.user = {
				provider: ctrl.accountProvider,
				username: ctrl.username,
				enabled: true,
				roles: ['ADMIN']
			};
			$state.go('fourth-step');
		};
	}
	
})();