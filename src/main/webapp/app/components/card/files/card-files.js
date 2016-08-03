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

        var uploader = ctrl.uploader = Card.getFileUploader(ctrl.card.id);

        uploader.onProgressItem = function(fileItem, progress) {
            console.info('onProgressItem', fileItem, progress);
        };

        // callback status
        uploader.onSuccessItem = function(fileItem, response, status, headers) {
            // remove from queue, upload success
        };
        uploader.onErrorItem = function(fileItem, response, status, headers) {
            // do something else lol
            console.info('onErrorItem', fileItem, response, status, headers);
        };
        uploader.onCancelItem = function(fileItem, response, status, headers) {
            // here do something else too
            console.info('onCancelItem', fileItem, response, status, headers);
        };

        // -----

        var loadFiles = function() {
            Card.files(card.id).then(function(files) {
                ctrl.files = {};
                for(var f = 0; f < files.length; f++) {
                    var file = files[f];
                    ctrl.files[file.cardDataId] = file;
                }
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
