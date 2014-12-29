(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgChart', function () {
		var baseWidth = 150;
		var baseHeight = 150;

		return {
			restrict: 'E',
			template: '<canvas></canvas>',
			scope: {
				data: "=data",
				options: "=options",
				type: "@",
				width: "@",
				height: "@"
			},
			link: function (scope, element, attrs) {
				scope.$watch('data', function (value) {
					if (value === undefined) {
						return;
					}

					var canvas = element.find('canvas')[0];
					var context = canvas.getContext('2d');

					canvas.width = scope.width || baseWidth;
					canvas.height = scope.height || baseHeight;
					context.canvas.style.maxHeight = canvas.height + "px";

					var chart = new Chart(context);
					var chartType = scope.type || "Line";
					chart[chartType](scope.data, scope.options);
				});
			}
		}
	});
})();
