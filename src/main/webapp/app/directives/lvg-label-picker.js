(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgLabelPicker', function (LabelCache) {
		return {
			templateUrl: 'partials/fragments/label-pickers.html',
			require: 'ngModel',
			scope: {
				model: '=ngModel',
				label: '=',
				board: '=',
				inMenu: '='
			},
			restrict: 'E',
			link: function (scope, element, attrs) {
				scope.internal = {};

				//hacky (?)
				scope.$watch('internal.model', function () {
					scope.model = scope.internal.model
				})
				
				scope.$watch('model', function() {
					scope.internal.model = scope.model;
				});


				scope.$watch('label', function () {
					scope.internal.model = null;
					scope.listValues = null;
					if (scope.label && scope.label.type === 'LIST') {
						LabelCache.findLabelListValues(scope.label.id).then(function (res) {
							scope.listValues = res;
						});
					}
				});
			}
		};
	});
})();



