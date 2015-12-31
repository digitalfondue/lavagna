(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsPanelBoard', {
		templateUrl : 'app/components/stats/panel-board/stats-panel-board.html',
		bindings : {
			board: '=',
			projectShortName: '='
		},
		controller : function(Board) {
			
			var ctrl = this;
			
			ctrl.statsFetcher = function() {
				return Board.taskStatistics(ctrl.board.shortName);
			}
		}
	});
})();
