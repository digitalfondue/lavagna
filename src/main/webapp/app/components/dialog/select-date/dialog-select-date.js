(function() {
	
	'use strict';
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectDate', {
			templateUrl: 'app/components/dialog/select-date/dialog-select-date.html',
			bindings: {
				dialogTitle: '<',
				action: '&'
			},
			controller: ['$mdDialog', dialogSelectDateCtrl]
		});
	
	function dialogSelectDateCtrl($mdDialog) {
		var ctrl = this;
		
		ctrl.cancel = cancel;
		ctrl.ok = ok;
		
		function cancel() {
			$mdDialog.hide();
		}
		
		function ok(date) {
			ctrl.action({'$date':date});
			$mdDialog.hide();
		}
	}
	
})();