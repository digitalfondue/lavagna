(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityComment', {
        bindings: {
            event: '<'
        },
        controller: [ActivityCommentController],
        templateUrl: 'app/components/activity/comment/activity-comment.html'
    });

    function ActivityCommentController() {
        var ctrl = this;
    }

})();
