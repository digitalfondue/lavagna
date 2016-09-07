(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityCardCreate', {
        bindings: {
            event: '<'
        },
        controller: ['BoardCache', ActivityCardCreateController],
        templateUrl: 'app/components/activity/card-create/activity-card-create.html'
    });

    function ActivityCardCreateController(BoardCache) {
        var ctrl = this;

        ctrl.$onInit = function() {
            BoardCache.column(ctrl.event.columnId).then(function(column) {
                ctrl.columnName = column.columnName;
            });
        }

    }
})();
