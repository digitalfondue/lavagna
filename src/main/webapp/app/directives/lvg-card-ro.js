(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardRo', function ($rootScope, CardCache) {

		var loadCard = function (cardId, shortNamePlaceholder, namePlaceholder, noName) {
			CardCache.card(cardId).then(function (card) {
				shortNamePlaceholder.text(card.boardShortName + '-' + card.sequence);
				if (card.columnDefinition != 'CLOSED') {
					shortNamePlaceholder.removeClass('lavagna-closed-card');
				} else {
					shortNamePlaceholder.addClass('lavagna-closed-card');
				}
				if (!noName) {
					namePlaceholder.text(card.name);
				}
			});
		};

		return {
			restrict: 'A',
			transclude: true,
			scope: true,
			template: '<span class="lavagna-card-short-placeholder"></span><span data-ng-transclude></span>' +
				' <span class="lavagna-card-name-placeholder"></span>',
			link: function ($scope, element, attrs) {
				var unregister = $scope.$watch(attrs.lvgCardRo, function (cardId) {
					if (cardId == undefined) {
						return;
					}
					var namePlaceholder = element.find('.lavagna-card-name-placeholder');
					var shortNamePlaceholder = element.find('.lavagna-card-short-placeholder');

					var noName = 'noName' in attrs;
					loadCard(cardId, shortNamePlaceholder, namePlaceholder, noName);

					var unbind = $rootScope.$on('refreshCardCache-' + cardId, function () {
						loadCard(cardId, shortNamePlaceholder, namePlaceholder, noName);
					});
					$scope.$on('$destroy', unbind);
					
					unregister();
				});
			}
		};
	});
})();
