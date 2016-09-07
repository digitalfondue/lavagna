(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityActionItem', {
        bindings: {
            event: '<'
        },
        controller: ['CardCache', ActivityActionItemController],
        templateUrl: 'app/components/activity/action-item/activity-action-item.html'
    });

    function ActivityActionItemController(CardCache) {
        var ctrl = this;

        ctrl.$onInit = function() {
            CardCache.cardData(ctrl.event.dataId).then(function(data) {
                ctrl.actionItem = data;
            });

            CardCache.cardData(ctrl.event.previousDataId).then(function(data) {
                ctrl.actionList = data;
            });
        }

    }
})();
