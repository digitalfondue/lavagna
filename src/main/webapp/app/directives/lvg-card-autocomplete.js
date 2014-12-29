(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardAutocomplete', function ($stateParams, $http, $parse) {
		return {
			restrict: 'A',
			link: function (scope, elem, attrs) {
				$(elem).autocomplete({
					minLength: 1,
					focus: function (event, ui) {
						event.preventDefault();
					},
					source: function (request, response) {
						
						var params = {term: request.term.trim()};
						if($stateParams.projectName) {
							params.projectName = $stateParams.projectName
						}
						
						$http.get('api/search/autocomplete-card', {params: params}).then(function (res) {
							response($.map(res.data, function (card) {
								return {
									label: card.boardShortName + "-" + card.sequence + " " + card.name,
									value: card
								};
							}));
						});
					},
					select: function (event, ui) {
						event.preventDefault();
						$(elem).val(ui.item.label);
						scope.$apply(function () {
							$parse(attrs['lvgCardAutocomplete']).assign(scope, ui.item.value);
						});
					}
				});
			}
		};
	});
})();