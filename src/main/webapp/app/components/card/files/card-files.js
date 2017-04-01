(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFiles', {
        bindings: {
            card: '&',
        },
        templateUrl: 'app/components/card/files/card-files.html',
        controller: ['StompClient', 'Card', CardFilesController],
    });

    function CardFilesController(StompClient, Card) {
        var ctrl = this;

        var card = ctrl.card();

        var onDestroyStomp = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.files = [];

            ctrl.uploader = Card.getFileUploader(card.id);

            // callback status
            ctrl.uploader.onSuccessItem = function (fileItem, response, status, headers) {
                ctrl.uploader.removeFromQueue(fileItem);
            };

            ctrl.uploader.onCancelItem = function (fileItem, response, status, headers) {
                ctrl.uploader.removeFromQueue(fileItem);
            };

            loadFiles();

            // the /card-data has various card data related event that are pushed from the server that we must react
            onDestroyStomp = StompClient.subscribe('/event/card/' + card.id + '/card-data', function (e) {
                var type = JSON.parse(e.body).type;

                if (type.match(/FILE$/g)) {
                    loadFiles();
                }
            });
        };

        ctrl.$onDestroy = function onDestroy() {
            onDestroyStomp();
        };

        // -----

        function loadFiles() {
            Card.files(card.id).then(function (files) {
                ctrl.files = files;
            });
        }
    }
}());
