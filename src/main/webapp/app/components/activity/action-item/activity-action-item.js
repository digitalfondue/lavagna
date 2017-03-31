(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityActionItem', {
        bindings: {
            event: '<'
        },
        controller: ['CardCache', ActivityActionItemController],
        template: '<div translate translate-values="{name: $ctrl.actionItem.content, list: $ctrl.actionList.content}">{{\'activity.action.item.\' + $ctrl.event.event}}</div>'
    });

    function ActivityActionItemController(CardCache) {
        var ctrl = this;

        ctrl.$onInit = function () {
            CardCache.cardData(ctrl.event.dataId).then(function (data) {
                ctrl.actionItem = data;
            });

            CardCache.cardData(ctrl.event.previousDataId).then(function (data) {
                ctrl.actionList = data;
            });
        };
    }
}());
