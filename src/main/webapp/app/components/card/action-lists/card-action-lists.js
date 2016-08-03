(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActionLists', {
        bindings: {
                    card: '<'
        },
        controller: CardActionListsController,
        templateUrl: 'app/components/card/action-lists/card-action-lists.html'
    });

    function CardActionListsController($scope, Card, StompClient) {
        var ctrl = this;

        var loadActionLists = function() {
            Card.actionLists(ctrl.card.id).then(function(actionLists) {
                ctrl.actionLists = [];

                for(var i = 0; i < actionLists.lists.length; i++) {
                    var actionList = actionLists.lists[i];
                    actionList.items = actionLists.items[actionList.id] || [];
                    actionList.items.referenceId = actionList.id;
                    ctrl.actionLists.push(actionList);
                }
            });
        };
        loadActionLists();

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
            Card.addActionList(ctrl.card.id, actionList.name).then(function() {
                actionList.name = null;
            });
        };

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe($scope, '/event/card/' + ctrl.card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;
            if(type.match(/ACTION_ITEM$/g) !== null || type.match(/ACTION_LIST$/g)) {
                loadActionLists();
                reloadCard();
            }
        });
    }

})();
