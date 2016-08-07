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
        
        ctrl.dropActionLists = function(index, oldIndex) {
        	var item = ctrl.actionLists[oldIndex];
        	
        	ctrl.actionLists.splice(oldIndex, 1);
        	ctrl.actionLists.splice(index, 0, item);
        	
        	ctrl.actionLists.forEach(function(v, i) {
        		v.order = i;
        	});
        	
        	var ids = ctrl.actionLists.map(function(v) {
        		return v.id;
        	});
        	
        	Card.updateActionListOrder(ctrl.card.id, ids).catch(function(err) {
        		ctrl.actionLists.splice(index, 1);
            	ctrl.actionLists.splice(oldIndex, 0, item);
            });
        }

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
            }
        });
    }

})();
