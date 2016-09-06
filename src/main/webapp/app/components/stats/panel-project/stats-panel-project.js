(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsPanelProject', {
		templateUrl : 'app/components/stats/panel-project/stats-panel-project.html',
		bindings : {
			project: '<'
		},
		controller : ['Project', StatsPanelProjectController]
	});
	
	
	function StatsPanelProjectController(Project) {
		
		var ctrl = this;
		
		ctrl.statsFetcher = statsFetcher;
		
		function statsFetcher() {
			return Project.taskStatistics(ctrl.project.shortName);
		}
	}
	
})();
