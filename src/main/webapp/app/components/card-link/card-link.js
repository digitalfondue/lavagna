/* eslint indent: 0 */
(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLink', {
        bindings: {
            cardId: '<'
        },
        controller: ['CardCache', CardLinkController],
        template: '<a ng-if="::$ctrl.card"  '
                      + ' ui-sref="board.card({projectName: $ctrl.card.projectShortName, shortName: $ctrl.card.boardShortName, seqNr: $ctrl.card.sequence})" '
                    + ' data-ng-class="::{\'lavagna-closed-card\': !$card.columnDefinition === \'CLOSED\'}"> '
                    + ' {{::($ctrl.card.boardShortName)}}-{{::($ctrl.card.sequence)}}'
                  + '</a> {{::($ctrl.card.name)}}'
    });

    function CardLinkController(CardCache) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            CardCache.card(ctrl.cardId).then(function (card) {
                ctrl.card = card;
            });
        };
    }
}());
