(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActivity', {
        bindings: {
            card: '<',
            user: '<'
        },
        controller: CardActivityController,
        templateUrl: 'app/components/card/activity/card-activity.html'
    });

    function CardActivityController($scope, $rootScope, $q, Card, StompClient) {
        var ctrl = this;
        ctrl.comments = {};
        ctrl.activities = [];

        var loadComments = function() {
            return Card.comments(ctrl.card.id);
        };

        var loadActivity = function() {
            return Card.activity(ctrl.card.id);
        };

        var loadData = function(promisesObject) {
            $q.all(promisesObject).then(function(result) {
                if(result.comments) {
                    angular.forEach(result.comments, function(comment) {
                        ctrl.comments[comment.id] = comment;
                    });
                }
                if(result.activities) {
                    ctrl.activities = result.activities;
                }
            })
        };

        loadData({comments: loadComments(), activities: loadActivity()});

        //--------------

        ctrl.addComment = function(comment) {
            Card.addComment(ctrl.card.id, comment).then(function() {
                comment.content = null;
            });
        };

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;

            var promisesObject = {activities: loadActivity()};
            if(type.match(/COMMENT/g) !== null) {
                promisesObject.comments = loadComments();
            }
            loadData(promisesObject);
        }, $scope);

        //reload activities when the card is moved/renamed
        var unbindCardCache = $rootScope.$on('refreshCardCache-' + ctrl.card.id, function() {
            loadData({activities: loadActivity()});
        });
        $scope.$on('$destroy', unbindCardCache);
    };
})();
