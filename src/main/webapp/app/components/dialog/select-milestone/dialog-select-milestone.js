(function() {
	
	'use strict';
	
	angular
		.module('lavagna.components')
		.component('lvgDialogSelectMilestone', {
			templateUrl: 'app/components/dialog/select-milestone/dialog-select-milestone.html',
			bindings: {
				action: '&', // $milestone
				projectName: '<'
			},
			controller: ['$mdDialog', '$element', '$filter', 'Project', DialogSelectMilestoneController]
		});
	
	function DialogSelectMilestoneController($mdDialog, $element, $filter, Project) {
		var ctrl = this;
		
		ctrl.ok = ok;
		ctrl.cancel = cancel;
		ctrl.reFilter = reFilter;
		
		ctrl.$onInit = function init() {
			//from the example https://material.angularjs.org/latest/demo/select
			$element.find('input').on('keydown', function(ev) {
				if(ev.keyCode !== 27) {//esc 
					ev.stopPropagation();
				}
			});
			
			ctrl.toSearch = '';
			
			Project.getMetadata(ctrl.projectName).then(function(res) {
				ctrl.open = [];
				ctrl.closed = [];
				angular.forEach(res.milestones, function(v) {
					if(v.status === 'CLOSED') {
						ctrl.closed.push(v);
					} else {
						ctrl.open.push(v);
					}
				});
				ctrl.reFilter();
			});
		};
		
		function reFilter() {
			ctrl.filteredOpen = $filter('filter')(ctrl.open, ctrl.toSearch);
			ctrl.filteredClosed = $filter('filter')(ctrl.closed, ctrl.toSearch);
		}
		
		function cancel() {
			$mdDialog.hide();
		}
		
		function ok(milestone) {
			ctrl.action({'$milestone' : milestone});
			$mdDialog.hide();
		}
	}
	
	
})();