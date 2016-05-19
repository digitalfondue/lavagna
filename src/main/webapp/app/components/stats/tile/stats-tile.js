(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsTile', {
		templateUrl : 'app/components/stats/tile/stats-tile.html',
		bindings : {
			tileTitle : "=",
			value : "=",
			subtitle : "=?",
			valueColor : "=?"
		},
		controller : function() {
			if (this.valueColor === undefined) {
				this.valueColor = 0;
			}
		}
	});
})();
