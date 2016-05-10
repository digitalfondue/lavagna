(function() {
	
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectDate', {
			templateUrl: 'app/components/dialog-select-date/dialog-select-date.html',
			bindings: {
				dialogTitle: '<',
				action: '='
			},
			controller: function($mdDialog) {
				var ctrl = this;
				
				ctrl.cancel = function() {
					$mdDialog.hide();
				}
				
				ctrl.ok = function(date) {
					if(ctrl.action) {ctrl.action(date);}
					$mdDialog.hide();
				}
			}
		})
	
})();