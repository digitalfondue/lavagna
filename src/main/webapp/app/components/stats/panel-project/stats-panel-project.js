(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsPanelProject', {
		templateUrl : 'app/components/stats/panel-project/stats-panel-project.html',
		bindings : {
			project: '<'
		},
		controller : function(Project) {
			
			var ctrl = this;

			ctrl.statsFetcher = function() {
				return Project.taskStatistics(ctrl.project.shortName);
			}
		}
	});
})();
