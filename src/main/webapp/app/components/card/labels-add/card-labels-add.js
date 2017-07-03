(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLabelsAdd', {
        bindings: {
            projectShortName: '<',
            userLabels: '<',
            labelValues: '<',
            onAdd: '&',
            onCancelAdd: '&'
        },
        controller: CardLabelsAddController,
        templateUrl: 'app/components/card/labels-add/card-labels-add.html'
    });

    function CardLabelsAddController() {
        var ctrl = this;

        ctrl.addNewLabel = function (labelToAdd) {
            ctrl.onAdd({$label: angular.copy(labelToAdd)});
        };

        ctrl.cancel = function () {
            ctrl.onCancelAdd();
        };
    }
}());
