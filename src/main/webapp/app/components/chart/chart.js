(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgChart', {
		template: '<canvas></canvas>',
		bindings: {
			data: "<",
			options: "<",
			type: "@",
			width: "@",
			height: "@"
		},
		controller: ['$element', chartCtrl]
	});
	
	
	function chartCtrl($element) {
		
		var ctrl = this;
		
		ctrl.$onChanges = function onChanges(changes) {
			if(changes.data || changes.options) {
				var value = ctrl.data;
				if (value === undefined) {
					return;
				}
				
				var baseWidth = 150;
				var baseHeight = 150;
				
				var canvas = $element.find('canvas')[0];
				var context = canvas.getContext('2d');

				canvas.width = ctrl.width || baseWidth;
				canvas.height = ctrl.height || baseHeight;
				context.canvas.style.maxHeight = canvas.height + "px";

				var chart = new Chart(context);
				var chartType = ctrl.type || "Line";
				chart[chartType](ctrl.data, ctrl.options);
			}
		}
	}
	
})();
