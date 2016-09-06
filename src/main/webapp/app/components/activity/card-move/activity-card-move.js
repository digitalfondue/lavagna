(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityCardMove', {
        bindings: {
            event: '<'
        },
        controller: ['BoardCache', ActivityCardMoveController],
        templateUrl: 'app/components/activity/card-move/activity-card-move.html'
    });

    function ActivityCardMoveController(BoardCache) {
        var ctrl = this;

        ctrl.$onInit = function() {
            BoardCache.column(ctrl.event.previousColumnId).then(function(column) {
                ctrl.columnFrom = column.columnName;
            });

            BoardCache.column(ctrl.event.columnId).then(function(column) {
                ctrl.columnTo = column.columnName;
            });
        }

    }
})();
