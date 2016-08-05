(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFiles', {
        bindings: {
            card: '<',
            user: '<'
        },
        controller: CardFilesController,
        templateUrl: 'app/components/card/files/card-files.html'
    });

    function CardFilesController($scope, StompClient, Card, Notification) {
        var ctrl = this;
        var card = ctrl.card;
        ctrl.files = [];

        var uploader = ctrl.uploader = Card.getFileUploader(ctrl.card.id);

        // callback status
        uploader.onSuccessItem = function(fileItem, response, status, headers) {
            uploader.removeFromQueue(fileItem);
        };

        uploader.onCancelItem = function(fileItem, response, status, headers) {
            uploader.removeFromQueue(fileItem);
        };

        // -----

        var loadFiles = function() {
            Card.files(card.id).then(function(files) {
                ctrl.files = files;
            });
        };

        loadFiles();

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe($scope, '/event/card/' + card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;
            if(type.match(/FILE$/g)) {
                loadFiles();
            }
        });
    };
})();
