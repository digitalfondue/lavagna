/* eslint indent: 0 */
(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardActivityComment', {
        bindings: {
            comment: '<'
        },
        controller: ['Card', 'Notification', CardActivityCommentController],
        template: '<div class="row row-inherit">'
                   + '<div class="lvg-card-comment__avatar">'
                   + '<lvg-user-avatar user-id="$ctrl.comment.userId"></lvg-user-avatar>'
                   + '</div>'
                   + '<div class="lvg-card-comment__container">'
                   + '<lvg-rebuild-if-change to-watch="$ctrl.comment.updatedCount" class="row row-inherit">'
                   + '<div class="lvg-card-comment__body" ng-if="!$ctrl.editComment">'
                   + '<div class="lvg-card-comment__headline row row-inherit start-xs middle-xs">'
                   + '<div class="flex no grow">'
                   + '<lvg-user-link class="lvg-card-activity__user" user-id="$ctrl.comment.userId"></lvg-user-link>'
                   + '<span class="lvg-card-activity__time">{{ ::($ctrl.comment.updateDate | dateIncremental) }}</span>'
                   + '<span class="lvg-card-comment__edited" ng-if="::$ctrl.comment.updatedCount">{{::(\'label.edited\' | translate)}}</span>'
                   + '</div>'
                   + '<div class="flex"></div>'
                   + '<div class="flex no-grow">'
                   + '<md-button data-lvg-has-permission="UPDATE_CARD_COMMENT" data-owner-id="$ctrl.comment.userId"'
                   + 'data-ng-click="$ctrl.commentEdit = $ctrl.comment.content; $ctrl.editComment = true;"'
                   + 'class="lvg-small-icon-button">'
                   + '<md-tooltip><span translate>card.comment.edit.tooltip</span></md-tooltip>'
                   + '<div class="lvg-icon-18 lvg-icon__edit"></div>'
                   + '</md-button>'
                   + '<md-button data-lvg-has-permission="DELETE_CARD_COMMENT" data-owner-id="$ctrl.comment.userId"'
                   + 'data-ng-click="$ctrl.delete()" class="lvg-small-icon-button">'
                   + '<md-tooltip><span translate>card.comment.delete.tooltip</span></md-tooltip>'
                   + '<div class="lvg-icon-18 lvg-icon__delete"></div>'
                   + '</md-button>'
                   + '</div>'
                   + '</div>'
                   + '<div class="lvg-card-comment__content"'
                   + 'data-ng-bind-html="::($ctrl.comment.content | markdown)">'
                   + '</div>'
                   + '</div>'
                   + '</lvg-rebuild-if-change>'
                   + '<div class="lvg-card-comment__edit flex column" ng-if="$ctrl.editComment">'
                   + '<div class="lvg-card-comment__edit-headline row row-inherit start-xs middle-xs">'
                   + '<div class="flex no grow">'
                   + '<lvg-user-link class="lvg-card-activity__user" user-id="$ctrl.comment.userId"></lvg-user-link>'
                   + '<span class="lvg-card-activity__time">{{ ::($ctrl.comment.updateDate | dateIncremental) }}</span>'
                   + '<span class="lvg-card-comment__edited" ng-if="::$ctrl.comment.updatedCount">{{::(\'label.edited\' | translate)}}</span>'
                   + '</div>'
                   + '<div class="flex"></div>'
                   + '</div>'
                   + '<div class="lvg-card-comment__edit-content">'
                   + '<form data-ng-submit="$ctrl.updateComment($ctrl.commentEdit); $ctrl.editComment = false; $ctrl.commentPreviewMode = false;">'
                   + '<div data-ng-hide="$ctrl.commentPreviewMode">'
                   + '<md-input-container class="md-block">'
                   + '<textarea class="lvg-card-activity__actions-textarea"'
                   + 'ng-model="$ctrl.commentEdit" rows="3" lvg-focus-on="$ctrl.editComment" lvg-on-esc="$ctrl.editComment = false; $ctrl.commentPreviewMode = false;"></textarea>'
                   + '</md-input-container>'
                   + '</div>'
                   + '<div data-ng-show="$ctrl.commentPreviewMode"'
                   + 'data-ng-bind-html="$ctrl.commentEdit | markdown"></div>'
                   + '<div class="lvg-card-comment__actions-button-row">'
                   + '<md-button class="md-primary" type="submit">'
                   + '<span data-translate>button.update</span>'
                   + '</md-button>'
                   + '<md-button data-ng-hide="$ctrl.commentPreviewMode"'
                   + 'data-ng-click="$ctrl.commentPreviewMode = true" type="button">'
                   + '<span data-translate>button.preview</span>'
                   + '</md-button>'
                   + '<md-button data-ng-show="$ctrl.commentPreviewMode"'
                   + 'data-ng-click="$ctrl.commentPreviewMode = false" type="button">'
                   + '<span data-translate>button.closePreview</span>'
                   + '</md-button>'
                   + '<md-button ng-click="$ctrl.editComment = false; $ctrl.commentPreviewMode = false;">'
                   + '<span data-translate>button.cancel</span>'
                   + '</md-button>'
                   + '</div>'
                   + '</form>'
                   + '</div>'
                   + '</div>'
                   + '</div>'
                   + '</div>'
    });

    function CardActivityCommentController(Card, Notification) {
        var ctrl = this;

        ctrl.updateComment = function (commentToEdit) {
            Card.updateComment(ctrl.comment.id, {content: commentToEdit});
        };

        ctrl.delete = function () {
            Card.deleteComment(ctrl.comment.id).then(function (event) {
                Notification.addNotification('success', { key: 'notification.card.COMMENT_DELETE.success'}, true, true, function (notification) {
                    Card.undoDeleteComment(event.id).then(notification.acknowledge);
                });
            }, function () {
                Notification.addAutoAckNotification('error', { key: 'notification.card.ACTION_ITEM_DELETE.error'}, false);
            });
        };
    }
}());
