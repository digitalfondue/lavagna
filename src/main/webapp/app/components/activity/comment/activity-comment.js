(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityComment', {
        bindings: {
            event: '<'
        },
        controller: ['CardCache', ActivityCommentController],
        templateUrl: 'app/components/activity/comment/activity-comment.html'
    });

    function ActivityCommentController(UserCache, CardCache) {
        var ctrl = this;

    }
})();
