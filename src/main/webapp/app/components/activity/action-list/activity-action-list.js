(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityActionList', {
        bindings: {
            event: '<'
        },
        controller: ['CardCache', ActivityActionListController],
        template: '<div translate translate-values="{name: $ctrl.actionList.content}">{{\'activity.action.list.\' + $ctrl.event.event}}</div>'
    });

    function ActivityActionListController(CardCache) {
        var ctrl = this;

        ctrl.$onInit = function () {
            CardCache.cardData(ctrl.event.dataId).then(function (data) {
                ctrl.actionList = data;
            });
        };
    }
}());
