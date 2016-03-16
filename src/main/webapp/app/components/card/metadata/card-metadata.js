(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardMetadata', {
        bindings: {
            card: '<',
            board: '<',
            project: '<',
            user: '<'
        },
        controller: CardMetadataController,
        templateUrl: 'app/components/card/metadata/card-metadata.html'
    });

    function CardMetadataController($rootScope, CardCache, Card, User, LabelCache, Label, StompClient,
        Notification, Board, BulkOperations) {
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

        ctrl.updateDescription = function(description) {
            Card.updateDescription(ctrl.card.id, description);
        };

        var loadColumn = function(columnId) {
            Board.column(columnId).then(function(col) {
                ctrl.column = col;
            });
        };
        loadColumn(ctrl.card.columnId);
    };
})();
