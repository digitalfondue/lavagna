(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityCardCreate', {
        bindings: {
            event: '<'
        },
        controller: ['BoardCache', ActivityCardCreateController],
        template: '<div translate translate-values="{card: $ctrl.event.valueString, column: $ctrl.columnName}">activity.card.create</div>'
    });

    function ActivityCardCreateController(BoardCache) {
        var ctrl = this;

        ctrl.$onInit = function () {
            BoardCache.column(ctrl.event.columnId).then(function (column) {
                ctrl.columnName = column.columnName;
            });
        };
    }
}());
