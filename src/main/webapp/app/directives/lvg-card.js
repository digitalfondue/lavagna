(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCard', function ($rootScope, $q, CardCache) {

        var generateTooltipHTML = function (card) {
            return '<div class=\"lavagna-tooltip\">' +
                '<div class=\"name\">' + card.name + '</div>' +
                '</div>';
        };

		var loadCard = function (cardId, linkPlaceholder, shortNamePlaceholder, namePlaceholder, noName) {
            var deferred = $q.defer();
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
				linkPlaceholder.attr('href', '#/' + card.projectShortName + '/' + card.boardShortName + '-' + card.sequence);

                deferred.resolve(generateTooltipHTML(card));
			});
            return deferred.promise;
		};

		return {
			restrict: 'A',
			transclude: true,
			scope: true,
			template: '<span data-bindonce="readOnly" data-lvg-tooltip data-lvg-tooltip-html="{{tooltipHTML}}">'
				+ '<span data-bo-if="!readOnly">'
				+	'<a class="lavagna-card-link-placeholder"><span class="lavagna-card-short-placeholder"></span><span data-ng-transclude></span></a> <span class="lavagna-card-name-placeholder"></span>'
				+ '</span><span data-bo-if="readOnly">'
				+	'<span class="lavagna-card-short-placeholder"></span><span data-ng-transclude></span> <span class="lavagna-card-name-placeholder"></span>'
				+ '</span></span>',
			link: function ($scope, element, attrs) {
				$scope.readOnly = attrs.readOnly != undefined;

				var unregister = $scope.$watch(attrs.lvgCard, function (cardId) {
					if (cardId == undefined) {
						return;
					}
					var linkPlaceholder = element.find('.lavagna-card-link-placeholder');
					var namePlaceholder = element.find('.lavagna-card-name-placeholder');
					var shortNamePlaceholder = element.find('.lavagna-card-short-placeholder');

					var noName = 'noName' in attrs;
					loadCard(cardId, linkPlaceholder, shortNamePlaceholder, namePlaceholder, noName).then(function (html) {
                        $scope.tooltipHTML = html;
                    });

					var unbind = $rootScope.$on('refreshCardCache-' + cardId, function () {
						loadCard(cardId, linkPlaceholder, shortNamePlaceholder, namePlaceholder, noName).then(function (html) {
                            $scope.tooltipHTML = html;
                        });
					});
					$scope.$on('$destroy', unbind);

					unregister();
				});
			}
		};
	});
})();
