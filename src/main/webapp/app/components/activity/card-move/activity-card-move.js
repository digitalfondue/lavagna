(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityCardMove', {
        bindings: {
            event: '<'
        },
        controller: ['BoardCache', ActivityCardMoveController],
        template: '<div translate translate-values="{from: $ctrl.columnFrom, to: $ctrl.columnTo}">activity.card.move</div>'
    });

    function ActivityCardMoveController(BoardCache) {
        var ctrl = this;

        ctrl.$onInit = function () {
            BoardCache.column(ctrl.event.previousColumnId).then(function (column) {
                ctrl.columnFrom = column.columnName;
            });

            BoardCache.column(ctrl.event.columnId).then(function (column) {
                ctrl.columnTo = column.columnName;
            });
        };
    }
}());
