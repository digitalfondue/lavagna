/* eslint indent: 0 */
(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivity', {
        bindings: {
            event: '<',
        },
        transclude: true,
        controller: [ActivityController],
        template: '<div class="flex column lvg-activity">'
                   + '<div class="row row-inherit start-xs middle-xs lvg-activity__header" ng-transclude></div>'
                   + '<div ng-switch="::$ctrl.event.event" class="lvg-activity__content">'
                   + '<div ng-switch-when="CARD_MOVE"><lvg-activity-card-move event="$ctrl.event"></lvg-activity-card-move></div>'
                   + '<div ng-switch-when="CARD_CREATE"><lvg-activity-card-create event="$ctrl.event"></lvg-activity-card-create></div>'
                   + '<div ng-switch-when="CARD_ARCHIVE"><span translate>activity.card.archive</span></div>'
                   + '<div ng-switch-when="CARD_BACKLOG"><span translate>activity.card.backlog</span></div>'
                   + '<div ng-switch-when="CARD_TRASH"><span translate>activity.card.trash</span></div>'
                   + '<div ng-switch-when="CARD_UPDATE"><span translate translate-values="{name: $ctrl.event.valueString}">activity.card.update</span></div>'
                   + '<div ng-switch-when="ACTION_LIST_CREATE"><lvg-activity-action-list event="$ctrl.event"></lvg-activity-action-list></div>'
                   + '<div ng-switch-when="ACTION_LIST_DELETE"><lvg-activity-action-list event="$ctrl.event"></lvg-activity-action-list></div>'
                   + '<div ng-switch-when="ACTION_ITEM_CREATE"><lvg-activity-action-item event="$ctrl.event"></lvg-activity-action-item></div>'
                   + '<div ng-switch-when="ACTION_ITEM_DELETE"><lvg-activity-action-item event="$ctrl.event"></lvg-activity-action-item></div>'
                   + '<div ng-switch-when="ACTION_ITEM_MOVE"><lvg-activity-action-item event="$ctrl.event"></lvg-activity-action-item></div>'
                   + '<div ng-switch-when="ACTION_ITEM_CHECK"><lvg-activity-action-item event="$ctrl.event"></lvg-activity-action-item></div>'
                   + '<div ng-switch-when="ACTION_ITEM_UNCHECK"><lvg-activity-action-item event="$ctrl.event"></lvg-activity-action-item></div>'
                   + '<div ng-switch-when="COMMENT_CREATE"><lvg-activity-comment event="$ctrl.event"></lvg-activity-comment></div>'
                   + '<div ng-switch-when="COMMENT_UPDATE"><lvg-activity-comment event="$ctrl.event"></lvg-activity-comment></div>'
                   + '<div ng-switch-when="COMMENT_DELETE"><lvg-activity-comment event="$ctrl.event"></lvg-activity-comment></div>'
                   + '<div ng-switch-when="FILE_UPLOAD"><span translate="activity.file.upload" translate-values="{name: $ctrl.event.valueString}">activity.file.upload</span></div>'
                   + '<div ng-switch-when="FILE_DELETE"><span translate translate-values="{name: $ctrl.event.valueString}">activity.file.delete</span></div>'
                   + '<div ng-switch-when="DESCRIPTION_CREATE"><span translate>activity.description.create</span></div>'
                   + '<div ng-switch-when="DESCRIPTION_UPDATE"><span translate>activity.description.update</span></div>'
                   + '<div ng-switch-when="LABEL_CREATE"><lvg-activity-label event="$ctrl.event"></lvg-activity-label></div>'
                   + '<div ng-switch-when="LABEL_DELETE"><lvg-activity-label event="$ctrl.event"></lvg-activity-label></div>'
                   + '</div>'
                   + '</div>'
    });

    function ActivityController() {}
}());
