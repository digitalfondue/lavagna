(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLabelsAdd', {
        bindings: {
            card: '<',
            project: '<',
            labelValues: '<',
            onCancelAdd: '&'
        },
        controller: CardLabelsAddController,
        templateUrl: 'app/components/card/labels-add/card-labels-add.html'
    });

    function CardLabelsAddController(BulkOperations, Label) {
        var ctrl = this;

        ctrl.userLabels = ctrl.project.metadata.userLabels;

        var currentCard = function () {
            var cardByProject = {};

            cardByProject[ctrl.project.shortName] = [ctrl.card.id];

            return cardByProject;
        };

        ctrl.addNewLabel = function (labelToAdd) {
            var labelValueToUpdate = Label.extractValue(labelToAdd.label, labelToAdd.value);

            BulkOperations.addLabel(currentCard(), labelToAdd.label, labelValueToUpdate);
        };

        ctrl.cancel = function () {
            ctrl.onCancelAdd();
        };
    }
}());
