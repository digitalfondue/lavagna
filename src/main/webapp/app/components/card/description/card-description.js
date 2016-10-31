(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardDescription', {
        bindings: {
            project: '<',
            card: '<',
            labelValues: '<'
        },
        templateUrl: 'app/components/card/description/card-description.html',
        controller: ['Card', 'StompClient', CardDescriptionController],
    });

    function CardDescriptionController(Card, StompClient) {
        var ctrl = this;

        ctrl.hideAddPanel = hideAddPanel;
        ctrl.updateCardName = updateCardName;
        ctrl.updateDescription = updateDescription;
        ctrl.hasUserLabels = hasUserLabels;

        //

        var onDestroyStomp = angular.noop;

        ctrl.$onInit = function init() {
        	loadDescription();

            //the /card-data has various card data related event that are pushed from the server that we must react
        	onDestroyStomp = StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function(e) {
                var type = JSON.parse(e.body).type;
                if(type === 'UPDATE_DESCRIPTION') {
                    loadDescription();
                }
            });
        };

        ctrl.$onDestroy = function onDestroy() {
        	onDestroyStomp();
        };

        // -----
        function updateCardName(newName) {
            Card.update(ctrl.card.id, newName);
        }

        function updateDescription(description) {
            Card.updateDescription(ctrl.card.id, description);
        }

        function hideAddPanel() {
            ctrl.addLabelPanel = false;
        }

        function loadDescription() {
            Card.description(ctrl.card.id).then(function(description) {
                ctrl.description = description;
            });
        }

        function hasUserLabels() {
            if(ctrl.project.metadata.userLabels === undefined || ctrl.labelValues === undefined) {
                return false;
            }

            for(var v in ctrl.project.metadata.userLabels) {
                if(ctrl.labelValues[ctrl.project.metadata.userLabels[v].id] !== undefined) {
                    return true;
                }
            }

            return false;
        }

    }

})();
