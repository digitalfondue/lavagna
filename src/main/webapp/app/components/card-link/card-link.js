(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLink', {
        bindings: {
            cardId: '<'
        },
        controller: CardLinkController,
        template: '<a ng-if="::$ctrl.card"  '
        	      	+ ' ui-sref="user.dashboard({provider: $ctrl.user.provider, username: $ctrl.user.username})" '
        		    + ' data-ng-class="::{\'lavagna-closed-card\': !$card.columnDefinition === \'CLOSED\'}"> '
        		    + ' {{::($ctrl.card.boardShortName)}}-{{::($ctrl.card.sequence)}}'
        		  +'</a> {{::($ctrl.card.name)}}'
    });

    function CardLinkController(CardCache) {
        var ctrl = this;

        CardCache.card(ctrl.cardId).then(function (card) {
        	ctrl.card = card;
        });
    };
})();
