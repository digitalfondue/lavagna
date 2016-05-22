(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsPanel', {
		templateUrl : 'app/components/stats/panel/stats-panel.html',
		bindings : {
			item: '<',
			statsFetcher: '<'
		},
		controller : function($filter) {
			
			var ctrl = this;

			function createNormalizedData(stats) {
				var normalizedStats = [];
				normalizedStats.push({value: stats.openTaskCount, color: $filter('color')(stats.openTaskColor).color});
				normalizedStats.push({value: stats.closedTaskCount, color: $filter('color')(stats.closedTaskColor).color});
				normalizedStats.push({value: stats.backlogTaskCount, color: $filter('color')(stats.backlogTaskColor).color});
				normalizedStats.push({value: stats.deferredTaskCount, color: $filter('color')(stats.deferredTaskColor).color});
				return normalizedStats;
			};

			function hasTasks(stats) {
				var totalTasks = parseInt(stats.openTaskCount) +
					parseInt(stats.closedTaskCount) +
					parseInt(stats.backlogTaskCount) +
					parseInt(stats.deferredTaskCount);
				return totalTasks > 0;
			};
			
			this.chartOptions = {animation : false};
			
			this.statsFetcher().then(function(stats) {
				if (stats === undefined) {
					return;
				}
				ctrl.statistics = stats;
				if (hasTasks(stats)) {
					ctrl.chartData = createNormalizedData(stats);
				}
			})
		}
	});
})();
