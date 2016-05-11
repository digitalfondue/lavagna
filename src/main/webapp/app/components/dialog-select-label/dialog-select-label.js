(function() {
	
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectLabel', {
			templateUrl: 'app/components/dialog-select-label/dialog-select-label.html',
			bindings: {
				dialogTitle: '<',
				action: '=',
			},
			controller: function($stateParams, $mdDialog, LabelCache) {
				var ctrl = this;
				
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
				
				ctrl.ok = function(date) {
					if(ctrl.action) {ctrl.action(date);}
					$mdDialog.hide();
				}
			}
		})
	
})();