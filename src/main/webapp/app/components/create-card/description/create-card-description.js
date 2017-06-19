(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCreateCardDescription', {
        bindings: {
            name: '<',
            description: '<',
            labels: '<',
            metadata: '<',
            onUpdate: '&'
        },
        controller: ['Label', CreateCardDescriptionController],
        templateUrl: 'app/components/create-card/description/create-card-description.html'
    });

    function CreateCardDescriptionController(Label) {
        var ctrl = this;
        ctrl.labelValues = {};

        ctrl.$onChanges = function (changes) {
            if (changes.metadata) {
                setUpProject();
            }

            if (changes.labels) {
                setUpLabels();
            }
        };

        ctrl.update = function () {
            ctrl.onUpdate({
                $name: ctrl.name,
                $description: ctrl.description,
                $labels: ctrl.labels
            });
        };

        ctrl.addLabel = function ($label) {
            var labelValue = Label.extractValue($label.label, $label.value);

            ctrl.labels.push({
                value: labelValue,
                labelId: $label.label.id,
                cardIds: []
            });

            setUpLabels();

            ctrl.update();
        };

        function setUpProject() {
            ctrl.project = {
                metadata: ctrl.metadata
            };
        }

        function setUpLabels() {
            var labelValues = {};

            angular.forEach(ctrl.labels, function (label) {
                if (labelValues[label.labelId] === undefined) {
                    labelValues[label.labelId] = [];
                }

                labelValues[label.labelId].push(label);
            });

            ctrl.labelValues = labelValues;
        }
    }
})();
