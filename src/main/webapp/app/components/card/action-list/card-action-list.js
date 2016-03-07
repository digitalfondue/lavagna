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
        controllerAs: 'alCtrl',
        templateUrl: 'app/components/card/action-list/card-action-list.html'
    });

    function CardActionListController(Card, Notification) {
        var ctrl = this;

        ctrl.addAction = function(name) {
            Card.addActionItem(ctrl.actionList.id, name).then(function() {
                name = null;
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
            console.log(action.type);
            Card.toggleActionItem(action.id, (action.type === 'ACTION_CHECKED'));
        };

        ctrl.sortActions = function(action, from, to, oldIndex, newIndex) {
            ctrl.actionList.items = to.map(function(v, i) {
                    v.order = i;
                    return v;
                });
            Card.updateActionItemOrder(
                ctrl.actionList.id,
                to.map(function(v) { return v.id})
                ).catch(function(err) {
                    ctrl.actionList.items = from;
                });
        };

        ctrl.saveName = function(name) {
            Card.updateActionList(ctrl.actionList.id, name);
        };

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
