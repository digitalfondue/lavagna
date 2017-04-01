(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActionLists', {
        bindings: {
            card: '&'
        },
        templateUrl: 'app/components/card/action-lists/card-action-lists.html',
        controller: ['Card', 'StompClient', CardActionListsController]
    });

    function CardActionListsController(Card, StompClient) {
        var ctrl = this;

        var card = ctrl.card();

        ctrl.addActionList = addActionList;
        ctrl.dropActionLists = dropActionLists;

        var onDestroyStomp = angular.noop;

        ctrl.$onInit = function init() {
            loadActionLists();

            // the /card-data has various card data related event that are pushed from the server that we must react
            onDestroyStomp = StompClient.subscribe('/event/card/' + card.id + '/card-data', function (e) {
                var type = JSON.parse(e.body).type;

                if (type.match(/ACTION_ITEM$/g) !== null || type.match(/ACTION_LIST$/g)) {
                    loadActionLists();
                }
            });
        };

        ctrl.$onDestroy = function onDestroy() {
            onDestroyStomp();
        };

        function loadActionLists() {
            Card.actionLists(card.id).then(function (actionLists) {
                ctrl.actionLists = [];

                for (var i = 0; i < actionLists.lists.length; i++) {
                    var actionList = actionLists.lists[i];

                    actionList.items = actionLists.items[actionList.id] || [];
                    actionList.items.referenceId = actionList.id;
                    ctrl.actionLists.push(actionList);
                }
            });
        }

        function dropActionLists(index, oldIndex) {
            var item = ctrl.actionLists[oldIndex];

            ctrl.actionLists.splice(oldIndex, 1);
            ctrl.actionLists.splice(index, 0, item);

            ctrl.actionLists.forEach(function (v, i) {
                v.order = i;
            });

            var ids = ctrl.actionLists.map(function (v) {
                return v.id;
            });

            Card.updateActionListOrder(card.id, ids).catch(function () {
                ctrl.actionLists.splice(index, 1);
                ctrl.actionLists.splice(oldIndex, 0, item);
            });
        }

        function addActionList(actionList) {
            Card.addActionList(card.id, actionList.name).then(function () {
                actionList.name = null;
            });
        }
    }
}());
