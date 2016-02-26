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
		controller: function (LabelCache, $scope, $stateParams, $http) {
			var ctrl = this;
			
			
			ctrl.searchCard = function(text) {
				var params = {term: text.trim()};
				if($stateParams.projectName) {
					params.projectName = $stateParams.projectName
				}
				
				return $http.get('api/search/autocomplete-card', {params: params}).then(function (res) {
					angular.forEach(res.data, function(card) {
						card.label = card.boardShortName + "-" + card.sequence + " " + card.name;
					});
					return res.data;
				});
			};
			
			
			
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



