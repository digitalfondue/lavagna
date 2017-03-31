/* eslint indent: 0 */
(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgActivityLabel', {
        bindings: {
            event: '<'
        },
        controller: [ActivityLabelController],
        template: '<div data-ng-switch="::$ctrl.event.labelName">'
                     + '<div data-ng-switch-when="MILESTONE">'
                     + '<span data-translate>{{::(\'activity.label.milestone.\' + $ctrl.event.event)}}</span> <lvg-label-val-v2 value-ref="$ctrl.event" project-metadata-ref="$ctrl.projectMetadata"></lvg-label-val-v2>'
                     + '</div>'
                     + '<div data-ng-switch-when="DUE_DATE">'
                     + '<span data-translate>{{::(\'activity.label.due.date.\' + $ctrl.event.event)}}</span> <lvg-label-val-v2 value-ref="$ctrl.event" project-metadata-ref="$ctrl.projectMetadata"></lvg-label-val-v2>'
                     + '</div>'
                     + '<div data-ng-switch-when="ASSIGNED">'
                     + '<span data-translate>{{::(\'activity.label.assigned.\' + $ctrl.event.event)}}</span> <lvg-label-val-v2 value-ref="$ctrl.event" project-metadata-ref="$ctrl.projectMetadata"></lvg-label-val-v2>'
                     + '</div>'
                     + '<div data-ng-switch-when="WATCHED_BY" data-translate>{{::(\'activity.label.watch.\' + $ctrl.event.event)}}</div>'
                     + '<div data-ng-switch-default>'
                     + '<span data-translate data-translate-values="{name: $ctrl.event.labelName}">{{::(\'activity.label.default.\' + $ctrl.event.event)}}</span> <lvg-label-v2 value-ref="$ctrl.event" project-metadata-ref="$ctrl.projectMetadata"></lvg-label-v2>'
                     + '</div>'
                     + '</div>'
    });

    function ActivityLabelController() {
    }
}());
