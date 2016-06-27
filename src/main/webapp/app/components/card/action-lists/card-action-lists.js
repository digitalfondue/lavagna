(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActionLists', {
        bindings: {
                    actionLists: '=',
                    card: '='
        },
        controller: CardActionListsController,
        templateUrl: 'app/components/card/action-lists/card-action-lists.html'
    });

    function CardActionListsController(Card) {
        var ctrl = this;

        ctrl.sortActionLists = function(actionlist, from, to, oldIndex, newIndex) {
            ctrl.actionLists = to.map(function(v, i) {
                    v.order = i;
                    return v;
                });
            Card.updateActionListOrder(
                ctrl.card.id,
                to.map(function(v) { return v.id})
                ).catch(function(err) {
                    ctrl.actionLists = from;
                });
        };

        ctrl.addActionList = function(actionList) {
            Card.addActionList(card.id, actionList).then(function() {
                actionList = null;
            });
        };
    }

})();
