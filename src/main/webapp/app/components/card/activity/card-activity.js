(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActivity', {
        bindings: {
            card: '<',
            project: '<',
            user: '<'
        },
        templateUrl: 'app/components/card/activity/card-activity.html',
        controller: ['EventBus', '$q', 'Card', 'StompClient', CardActivityController]
    });

    function CardActivityController(EventBus, $q, Card, StompClient) {
        var ctrl = this;

        ctrl.activityFilterValue = 'COMMENT';

        function activityFilter (activity, index, activities) {
            if(ctrl.activityFilterValue === null) {
                return true;
            }

            return activity.cardEvent === ctrl.activityFilterValue && ctrl.comments[activity.dataId] !== undefined;
        }

        //
        ctrl.addComment = addComment;

        ctrl.activityFilter = activityFilter;
        //

        var stompSubscription = angular.noop;
        var unbindCardCache = angular.noop;

        ctrl.$onInit = function init() {
        	ctrl.comments = {};
            ctrl.activities = [];

            loadData({comments: loadComments(), activities: loadActivity()});


            //the /card-data has various card data related event that are pushed from the server that we must react
            stompSubscription = StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function(e) {
                var type = JSON.parse(e.body).type;

                var promisesObject = {activities: loadActivity()};
                if(type.match(/COMMENT/g) !== null) {
                    promisesObject.comments = loadComments();
                }
                loadData(promisesObject);
            });

            // reload activities when the card is moved/renamed
            unbindCardCache = EventBus.on('refreshCardCache-' + ctrl.card.id, function() {
                loadData({activities: loadActivity()});
            });
        }

        ctrl.$onDestroy = function onDestroy() {
        	stompSubscription();
        	unbindCardCache();
        }

        function loadComments() {
            return Card.comments(ctrl.card.id);
        }

        function loadActivity() {
            return Card.activity(ctrl.card.id);
        }

        function loadData(promisesObject) {
            $q.all(promisesObject).then(function(result) {
                if(result.comments) {
                    ctrl.comments = {};
                    angular.forEach(result.comments, function(comment) {
                        ctrl.comments[comment.id] = comment;
                    });
                }
                if(result.activities) {
                    ctrl.activities = [];
                    angular.forEach(result.activities, function(activity) {
                        activity.cardEvent = activity.event === 'COMMENT_CREATE' && ctrl.comments[activity.dataId] !== undefined ?
                            'COMMENT' :
                            activity.event;
                        ctrl.activities.push(activity);
                    });
                }
            })
        }

        function addComment(comment) {
            Card.addComment(ctrl.card.id, comment).then(function() {
                comment.content = null;
            });
        }
    };
})();
