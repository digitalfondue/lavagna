(function () {

	'use strict';

	angular.module('lavagna.components').component('lvgSwitch', {
		template: "<div class=\"{{lvgSwitch.switchClass}}\" ng-class=\"{\'active\': lvgSwitch.model}\" ng-click=\"lvgSwitch.handleChange()\"><div class=\"button\"></div></div>",
		bindings: {
			model: '=control',
			change: '=',
			switchClass: '@',
			identifier: '@'
		},
		controller: function() {
			var ctrl = this;
			ctrl.handleChange = function() {
			    ctrl.model = !ctrl.model;
			    if(ctrl.change != undefined && ctrl.change != null) {
			    	ctrl.change(ctrl.model, ctrl.identifier);
			    }
			}
		}
		
	})
	
})();