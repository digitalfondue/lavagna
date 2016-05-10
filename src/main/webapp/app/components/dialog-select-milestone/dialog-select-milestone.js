(function() {
	
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectMilestone', {
			templateUrl: 'app/components/dialog-select-milestone/dialog-select-milestone.html',
			bindings: {
				dialogTitle: '<',
				action: '=',
			},
			controller: function($stateParams, $mdDialog, LabelCache) {
				var ctrl = this;
				
				LabelCache.findByProjectShortName($stateParams.projectName).then(function(res) {						
					for(var k in res) {
	  					if(res[k].domain === 'SYSTEM' && res[k].name === 'MILESTONE') {
	  						return res[k];
	  					}
					}
				}).then(function(label) {
					LabelCache.findLabelListValues(label.id).then(function (res) {
						ctrl.milestones = res;
					});
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