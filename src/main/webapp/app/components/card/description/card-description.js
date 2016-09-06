(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardDescription', {
        bindings: {
            project: '<',
            card: '<',
            labelValues: '<'
        },
        templateUrl: 'app/components/card/description/card-description.html',
        controller: ['EventBus', 'BulkOperations', 'Card', 'StompClient', CardDescriptionController],
    });

    function CardDescriptionController(EventBus, BulkOperations, Card, StompClient) {
        var ctrl = this;
        
        ctrl.hideAddPanel = hideAddPanel;
        ctrl.updateCardName = updateCardName;
        ctrl.updateDescription = updateDescription;
        
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
        }
        
        ctrl.$onDestroy = function onDestroy() {
        	onDestroyStomp();
        }

        // -----
        function updateCardName(newName) {
            Card.update(ctrl.card.id, newName).then( function() {
            	EventBus.emit('card.renamed.event');
            });
        };

        function updateDescription(description) {
            Card.updateDescription(ctrl.card.id, description);
        };

        function hideAddPanel() {
            ctrl.addLabelPanel = false;
        };

        function loadDescription() {
            Card.description(ctrl.card.id).then(function(description) {
                ctrl.description = description;
            });
        };

    }

})();
