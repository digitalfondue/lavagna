(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('AdminExportImportCtrl', function ($scope, $window, Notification, Admin) {

		$scope.overrideConfiguration = false;


		$scope.doExport = function (exportConf) {
			$("#export-iframe").remove();
			$($window.document.body).append('<iframe id="export-iframe" style="display: none !important"></iframe>');
			$window.document.getElementById('export-iframe').contentWindow.document.write('<html><head><base href="' + $('base').attr('href') + '"></head><body><form action="api/export" method="POST">'
				+ '<input type="hidden" name="_csrf" value="' + $window.csrfToken + '"></form>'
				+ '<script>document.forms[0].submit();</script></body></html>');
		}
		
		function importLavagnaCleanUp() {
			$scope.importFile = null;

			$scope.$apply(function () {
				$scope.importing = false;
			});
		}

		function importLavagna(data, status) {
			if(status == 200) {
				notifySuccess();
			} else {
				notifyError();
			}
			importLavagnaCleanUp();
		}
		
		function importLavagnaError() {
			notifyError();
			importLavagnaCleanUp();
		}
		
		function notifySuccess() {
			Notification.addAutoAckNotification('success', {
				key: 'notification.admin-export-import.import.success'
			}, false);
		}
		
		function notifyError() {
			Notification.addAutoAckNotification('error', {
				key: 'notification.admin-export-import.import.error'
			}, false);
		}
		
		$scope.importFile = null;
		$scope.onFileSelect = function($files) {
			$scope.importFile = $files[0]; //single file
		}

		$scope.doImport = function () {

			if ($scope.importFile == null) {
				return;
			}
			
			Admin.importData($scope.importFile, $scope.overrideConfiguration, function() {}, importLavagna, importLavagnaError);
			$scope.importing = true;
		}
	});
})();