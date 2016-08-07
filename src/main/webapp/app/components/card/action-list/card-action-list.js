(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActionList', {
        bindings: {
            card: '=',
            actionList: '='
        },
        transclude: {
            handle: 'handle'
        },
        controller: CardActionListController,
        templateUrl: 'app/components/card/action-list/card-action-list.html'
    });
    
    var dndActionListCtrl;
    var dndActionListItem;

    function CardActionListController(Card, Notification) {
        var ctrl = this;

        ctrl.addAction = function(action) {
            Card.addActionItem(ctrl.actionList.id, action.name).then(function() {
                action.name = null;
            });
        };

        ctrl.deleteAction = function(action) {
            Card.deleteActionItem(action.id).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.ACTION_ITEM_DELETE.success'}, true, true, function(notification) {
                    Card.undoDeleteActionItem(event.id).then(notification.acknowledge);
                });
            }, function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
            });
        };

        ctrl.toggleAction = function(action) {
            Card.toggleActionItem(action.id, (action.type === 'ACTION_CHECKED'));
        };
        
        
        ctrl.dragStartActionList = function(item) {
        	dndActionListItem = item;
        	dndActionListCtrl = ctrl;
        }
        
        ctrl.dropActionList = function(index, oldIndex) {
        	
        	var item = dndActionListCtrl.actionList.items[oldIndex];
        	
        	dndActionListCtrl.actionList.items.splice(oldIndex, 1);
        	ctrl.actionList.items.splice(index, 0, item);
        	
        	if(ctrl === dndActionListCtrl) {
            	ctrl.actionList.items.forEach(function(v, i) {
            		v.order = i;
            	});
            	Card.updateActionItemOrder(ctrl.actionList.id, ctrl.actionList.items.map(function(v) {return v.id}));
        	} else {
        		Card.moveActionItem(item.id, ctrl.actionList.id, {newContainer: ctrl.actionList.items.map(function(v) {return v.id})});
        	}
        }

        ctrl.saveName = function(name) {
            Card.updateActionList(ctrl.actionList.id, name);
        };

        ctrl.saveAction = function(id, content) {
            Card.updateActionItem(id, content);
        }

        ctrl.deleteList = function() {
            Card.deleteActionList(ctrl.actionList.id).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.ACTION_LIST_DELETE.success'}, true, true, function(notification) {
                    Card.undoDeleteActionList(event.id).then(notification.acknowledge)
                });
            }, function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_LIST_DELETE.success'}, false);
            });
        };
    };
})();
