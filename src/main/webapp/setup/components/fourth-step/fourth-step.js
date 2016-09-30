(function() {
	
	var module = angular.module('lavagna-setup');
	
	module.component('setupFourthStep', {
		controller: SetupActivationCtrl,
		templateUrl: 'components/fourth-step/fourth-step.html'
	});
	
	
	function SetupActivationCtrl($window, $http, $state) {
		
		var ctrl = this;
		
		ctrl.activate = function () {
			var configToUpdate = [{
				first: 'SETUP_COMPLETE',
				second: 'true'
			}].concat(ctrl.toSave.first, ctrl.toSave.second);
			$http.post('api/setup/', {toUpdateOrCreate: configToUpdate, user: ctrl.toSave.user}).then(goToRootApp);
		};
	}
	
})();