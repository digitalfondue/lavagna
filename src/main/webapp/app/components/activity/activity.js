(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivity', {
        bindings: {
            event: '<',
            project: '<'
        },
        transclude: true,
        controller: [ActivityController],
        templateUrl: 'app/components/activity/activity.html'
    });

    function ActivityController() {
        var ctrl = this;
    }
})();
