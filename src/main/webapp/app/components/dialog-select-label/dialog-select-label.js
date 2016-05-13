(function() {
	
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectLabel', {
			templateUrl: 'app/components/dialog-select-label/dialog-select-label.html',
			bindings: {
				dialogTitle: '<',
				action: '=',
				withLabelValuePicker: '<'
			},
			controller: function($stateParams, $mdDialog, LabelCache) {
				var ctrl = this;
				
				ctrl.selectedLabel = {};
				
				LabelCache.findByProjectShortName($stateParams.projectName).then(function(res) {
					ctrl.userLabels = [];
					for(var k in res) {
        		  		if(res[k].domain === 'USER') {
        		  			ctrl.userLabels.push(res[k]);
        		  		}
        		  	}
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