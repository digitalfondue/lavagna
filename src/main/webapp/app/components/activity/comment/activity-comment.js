(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityComment', {
        bindings: {
            event: '<'
        },
        controller: [ActivityCommentController],
        template: '<span translate>{{\'activity.comment.\' + $ctrl.event.event}}</span>'
    });

    function ActivityCommentController() {
        var ctrl = this;
    }

})();
