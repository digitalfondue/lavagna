(function() {
	
	var module = angular.module('lavagna-setup');
	
	module.component('setupFirstStep', {
		controller: SetupCtrl,
		templateUrl: 'components/first-step/first-step.html'
	});
	
	function SetupCtrl($window, $rootScope, $http, $state) {
		
		var ctrl = this;

		$rootScope.fromFirstStep = true;

		ctrl.baseUrlPlaceholder = getOrigin($window) + window.location.pathname.replace(/setup\/$/, '');

		if ($rootScope.toSave.first) {
			ctrl.baseUrl = $rootScope.toSave.first[0].second;
		} else {
			ctrl.baseUrl = ctrl.baseUrlPlaceholder;
		}


		$http.get('').then(function () {
			ctrl.submitImport = function () {
				var fd = new FormData();
				fd.append("overrideConfiguration", ctrl.overrideConfiguration)
				fd.append("file", document.getElementById('import-lavagna').files[0]);
				var xhr = new XMLHttpRequest();
				xhr.open("POST", 'api/import');
				xhr.addEventListener("load", goToRootApp, false);
				xhr.addEventListener("error", goToRootApp, false);
				xhr.addEventListener("abort", goToRootApp, false);
				xhr.setRequestHeader("x-csrf-token", window.csrfToken);
				xhr.send(fd);
			};
		});

		ctrl.submitBaseUrl = function () {
			//var config
			//add '/' at the end if missing
			var baseUrl = /\/$/.test(ctrl.baseUrl) ? ctrl.baseUrl : (ctrl.baseUrl + "/");

			$rootScope.toSave.first = [{first: 'BASE_APPLICATION_URL', second: baseUrl}];
			//$rootScope.oauth.baseUrl = baseUrl;
			$state.go('second-step');
		}
	}
	
	
	function getOrigin(window) {
		if (!window.location.origin) {
			window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
		}
		return window.location.origin;
	}
	
	
})();