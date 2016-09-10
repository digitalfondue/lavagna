(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsPanel', {
		templateUrl : 'app/components/stats/panel/stats-panel.html',
		bindings : {
			item: '<',
			statsFetcher: '<'
		},
		controller : ['$filter', StatsPanelController] 
	});
	
	function StatsPanelController($filter) {
		
		var ctrl = this;
		
		var colorFilter = $filter('color');
		
		ctrl.$onInit = function init() {
			ctrl.chartOptions = {animation : false};
			
			ctrl.statsFetcher().then(function(stats) {
				if (stats === undefined) {
					return;
				}
				ctrl.statistics = stats;
				if (hasTasks(stats)) {
					ctrl.chartData = createNormalizedData(stats);
				}
			})
		}

		function createNormalizedData(stats) {
			var normalizedStats = [];
			normalizedStats.push({value: stats.openTaskCount, color: colorFilter(stats.openTaskColor).color});
			normalizedStats.push({value: stats.closedTaskCount, color: colorFilter(stats.closedTaskColor).color});
			normalizedStats.push({value: stats.backlogTaskCount, color: colorFilter(stats.backlogTaskColor).color});
			normalizedStats.push({value: stats.deferredTaskCount, color: colorFilter(stats.deferredTaskColor).color});
			return normalizedStats;
		}

		function hasTasks(stats) {
			var totalTasks = parseInt(stats.openTaskCount) +
				parseInt(stats.closedTaskCount) +
				parseInt(stats.backlogTaskCount) +
				parseInt(stats.deferredTaskCount);
			return totalTasks > 0;
		}
	}
})();
