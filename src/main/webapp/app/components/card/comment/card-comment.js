(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardComment', {
        bindings: {
            comment: '<',
            card: '<'
        },
        controller: CardCommentController,
        templateUrl: 'app/components/card/comment/card-comment.html'
    });

    function CardCommentController(Card, Notification, UserCache) {
        var ctrl = this;

        UserCache.user(ctrl.comment.userId).then(function (user) {
            ctrl.user = user;
        });

        //
        ctrl.updateComment = function(commentToEdit) {
            Card.updateComment(ctrl.comment.id, {content: commentToEdit});
        };
        ctrl.delete = function() {
            Card.deleteComment(ctrl.comment.id).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.COMMENT_DELETE.success'}, true, true, function(notification) {
                    Card.undoDeleteComment(event.id).then(notification.acknowledge)
                });
            }, function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
            });
        };

    };
})();
