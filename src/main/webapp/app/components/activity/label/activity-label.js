(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityLabel', {
        bindings: {
            event: '<',
            project: '<'
        },
        controller: [ActivityLabelController],
        templateUrl: 'app/components/activity/label/activity-label.html'
    });

    function ActivityLabelController() {
        var ctrl = this;
    }

})();
