(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardDescription', {
        bindings: {
            project: '<',
            card: '<'
        },
        controller: CardDescriptionController,
        templateUrl: 'app/components/card/description/card-description.html'
    });

    function CardDescriptionController($rootScope, $scope, Card, StompClient) {
        var ctrl = this;

        // -----
        ctrl.updateCardName = function(newName) {
            Card.update(ctrl.card.id, newName).then( function() {
                $rootScope.$emit('card.renamed.event');
            });
        };

        // -----
        var loadDescription = function() {
            Card.description(ctrl.card.id).then(function(description) {
                ctrl.description = description;
            });
        };

        loadDescription();

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe($scope, '/event/card/' + ctrl.card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;
            if(type === 'UPDATE_DESCRIPTION') {
                loadDescription();
            }
        });
    }

})();
