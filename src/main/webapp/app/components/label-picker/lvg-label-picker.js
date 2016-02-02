(function () {

	'use strict';


	angular.module('lavagna.components').component('lvgLabelPicker', {
		templateUrl: 'app/components/label-picker/label-picker.html',
		bindings: {
			model: '=ngModel',
			label: '=',
			board: '=',
			inMenu: '=',
			group: '='
		},
		controllerAs: 'lvgLabelPicker',
		controller: function (LabelCache, $scope) {
			var ctrl = this;
			
			$scope.$watch('lvgLabelPicker.label', function () {
				ctrl.model = null;
				ctrl.listValues = null;
				if (ctrl.label && ctrl.label.type === 'LIST') {
					LabelCache.findLabelListValues(ctrl.label.id).then(function (res) {
						ctrl.listValues = res;
					});
				}
			});
		}
	});
	
})();



