(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityComment', {
        bindings: {
            event: '<'
        },
        controller: [ActivityCommentController],
        template: '<div translate>{{\'activity.comment.\' + $ctrl.event.event}}</div>'
    });

    function ActivityCommentController() {
    }
}());
