(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgChart', {
		template: '<canvas></canvas>',
		bindings: {
			data: "=",
			options: "=",
			type: "@",
			width: "@",
			height: "@"
		},
		controller: function($element, $scope) {
			
			var ctrl = this;
			
			ctrl.$postLink = function() {
				
				var baseWidth = 150;
				var baseHeight = 150;
				
				var canvas = $element.find('canvas')[0];
				var context = canvas.getContext('2d');
				
				$scope.$watch('$ctrl.data', function (value) {
					if (value === undefined) {
						return;
					}

					canvas.width = ctrl.width || baseWidth;
					canvas.height = ctrl.height || baseHeight;
					context.canvas.style.maxHeight = canvas.height + "px";

					var chart = new Chart(context);
					var chartType = ctrl.type || "Line";
					chart[chartType](ctrl.data, ctrl.options);
				});
			}
		}
	});
})();
