(function() {
	
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectLabel', {
			templateUrl: 'app/components/dialog/select-label/dialog-select-label.html',
			bindings: {
				dialogTitle: '<',
				buttonLabel:'<',
				action: '=',
				withLabelValuePicker: '<',
				projectName: '<'
			},
			controller: function($mdDialog, Project, $filter) {
				var ctrl = this;
				
				ctrl.selectedLabel = {};
				
				Project.getMetadata(ctrl.projectName).then(function(res) {
					ctrl.userLabels = $filter('orderBy')(res.userLabels, 'name'); 
				});
				
				ctrl.cancel = function() {
					$mdDialog.hide();
				}
				
				ctrl.ok = function(label, value) {
					if(ctrl.action) {ctrl.action(label, value);}
					$mdDialog.hide();
				}
			}
		})
	
})();