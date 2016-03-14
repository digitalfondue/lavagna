(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardTooltip', function ($rootScope, $q, CardCache) {
		
		//TODO: this is a temporary fix
		/*function escapeHtml(unsafe) {
		    return unsafe
		         .replace(/&/g, "&amp;")
		         .replace(/</g, "&lt;")
		         .replace(/>/g, "&gt;")
		         .replace(/"/g, "&quot;")
		         .replace(/'/g, "&#039;");
		 }*/

        var generateTooltipHTML = function (card) {
        	return '';
            /*return '<div class=\"lavagna-tooltip\">' +
                '<div class=\"name\">' + escapeHtml(card.name) + '</div>' +
                '</div>';*/
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
				linkPlaceholder.attr('href', '/' + card.projectShortName + '/' + card.boardShortName + '-' + card.sequence);

                deferred.resolve(generateTooltipHTML(card));
			});
            return deferred.promise;
		};

		return {
			restrict: 'A',
			scope: true,
			template: function($element, $attrs) {
				var readOnly = $attrs.readOnly != undefined;
				if(readOnly) {
					return '<span>'
					+ '<span class="lavagna-card-short-placeholder"></span> <span class="lavagna-card-name-placeholder"></span>'
					+ '</span>'
				} else {
					return '<span>'
					+	'<a class="lavagna-card-link-placeholder"><span class="lavagna-card-short-placeholder"></span></a> <span class="lavagna-card-name-placeholder"></span>'
					+ '</span>'
				}
				
				
			},
			link: function ($scope, element, attrs) {

				var unregister = $scope.$watch(attrs.lvgCardTooltip, function (cardId) {
					if (cardId == undefined) {
						return;
					}
					var linkPlaceholder = element.find('.lavagna-card-link-placeholder');
					var namePlaceholder = element.find('.lavagna-card-name-placeholder');
					var shortNamePlaceholder = element.find('.lavagna-card-short-placeholder');

					var noName = 'noName' in attrs;
					loadCard(cardId, linkPlaceholder, shortNamePlaceholder, namePlaceholder, noName).then(function (html) {
                        //$scope.tooltipHTML = html;
                    });

					var unbind = $rootScope.$on('refreshCardCache-' + cardId, function () {
						loadCard(cardId, linkPlaceholder, shortNamePlaceholder, namePlaceholder, noName).then(function (html) {
                            //$scope.tooltipHTML = html;
                        });
					});
					$scope.$on('$destroy', unbind);

					unregister();
				});
			}
		};
	});
})();
