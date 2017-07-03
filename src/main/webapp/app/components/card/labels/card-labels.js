(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLabels', {
        bindings: {
            card: '<',
            project: '<',
            labelValues: '<',
            onRemove: '&'
        },
        controller: CardLabelsController,
        templateUrl: 'app/components/card/labels/card-labels.html'
    });

    function CardLabelsController() {
        var ctrl = this;

        ctrl.$onInit = function init() {
            ctrl.labelValuesCntUpdate = 0;
        };

        ctrl.$onChanges = function onChanges(changes) {
            if (changes.labelValues) {
                ctrl.labelValuesCntUpdate++;
            }
        };
    }
}());
