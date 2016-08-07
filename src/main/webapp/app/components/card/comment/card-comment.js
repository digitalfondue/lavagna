(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardComment', {
        bindings: {
            comment: '<'
        },
        controller: CardCommentController,
        templateUrl: 'app/components/card/comment/card-comment.html'
    });

    function CardCommentController(Card, Notification) {
        var ctrl = this;

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
