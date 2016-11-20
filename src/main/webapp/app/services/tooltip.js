(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	//simple wrapper
	services.factory('Tooltip', ['$mdPanel', Tooltip]);


	function Tooltip($mdPanel) {
		return {
			clean: function(ignore) {
				angular.forEach($mdPanel._trackedPanels, function(value, id) {
                    if(id !== ignore && id.indexOf('lvg-tooltip') === 0) {
                        value.close();
                    }
                });
			}
		}
	}

})();
