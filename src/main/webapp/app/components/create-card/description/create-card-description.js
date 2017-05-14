(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCreateCardDescription', {
        bindings: {
            name: '<',
            description: '<',
            labels: '<',
            userLabels: '<',
            onUpdate: '&'
        },
        controller: CreateCardDescriptionController,
        templateUrl: 'app/components/create-card/description/create-card-description.html'
    });

    function CreateCardDescriptionController() {
        var ctrl = this;

        ctrl.update = function () {
            ctrl.onUpdate({
                $name: ctrl.name,
                $description: ctrl.description,
                $labels: ctrl.labels
            });
        };

        ctrl.addLabel = function ($label) {
            ctrl.labels.push($label);

            ctrl.update();
        };
    }
})();
