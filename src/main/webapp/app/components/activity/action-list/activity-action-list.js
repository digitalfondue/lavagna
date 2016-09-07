(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityActionList', {
        bindings: {
            event: '<'
        },
        controller: ['CardCache', ActivityActionListController],
        templateUrl: 'app/components/activity/action-list/activity-action-list.html'
    });

    function ActivityActionListController(CardCache) {
        var ctrl = this;

        ctrl.$onInit = function() {
            CardCache.cardData(ctrl.event.dataId).then(function(data) {
                ctrl.actionList = data;
            });
        }

    }
})();
