(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardSearchResult', function () {
		return {
			templateUrl: 'partials/fragments/card-search-result-fragment.html',
			restrict: 'E',
			controller: function ($scope) {
				$scope.hasUserLabels = function (cardLabels) {
					if (cardLabels === undefined || cardLabels.length === 0) {
						return false; //empty, no labels at all
					}
					for (var i = 0; i < cardLabels.length; i++) {
						if (cardLabels[i].labelDomain == 'USER') {
							return true;
						}
					}
					return false;
				};

				$scope.hasSystemLabelByName = function (labelName, cardLabels) {
					if (cardLabels === undefined || cardLabels.length === 0)
						return false; //empty, no labels at all
					for (var i = 0; i < cardLabels.length; i++) {
						if (cardLabels[i].labelName == labelName && cardLabels[i].labelDomain == 'SYSTEM') {
							return true;
						}
					}
					return false;
				};
			}
		}
	});
})();