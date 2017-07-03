(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFiles', {
        bindings: {
            card: '&',
        },
        templateUrl: 'app/components/card/files/card-files.html',
        controller: ['StompClient', 'Card', 'Notification', CardFilesController],
    });

    function CardFilesController(StompClient, Card, Notification) {
        var ctrl = this;

        var card = ctrl.card();

        var onDestroyStomp = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.files = [];

            ctrl.uploader = Card.getFileUploader(card.id);

            // callback status
            ctrl.uploader.onSuccessItem = function (fileItem) {
                ctrl.uploader.removeFromQueue(fileItem);
            };

            ctrl.uploader.onCancelItem = function (fileItem) {
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

        ctrl.delete = function ($file) {
            Card.deleteFile($file.cardDataId).then(function (event) {
                Notification.addNotification('success', {key: 'notification.card.FILE_DELETE.success'}, true, true, function (notification) {
                    Card.undoDeleteFile(event.id).then(notification.acknowledge);
                });
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.card.FILE_DELETE.error'}, false);
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
