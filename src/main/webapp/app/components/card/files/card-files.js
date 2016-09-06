(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFiles', {
        bindings: {
            card: '<',
            user: '<'
        },
        templateUrl: 'app/components/card/files/card-files.html',
        controller: ['StompClient', 'Card', 'Notification', CardFilesController],
    });

    function CardFilesController(StompClient, Card, Notification) {
        var ctrl = this;
        
        var onDestroyStomp = angular.noop;
        
        ctrl.$onInit = function init() {
        	ctrl.files = [];

            ctrl.uploader = Card.getFileUploader(ctrl.card.id);

            // callback status
            ctrl.uploader.onSuccessItem = function(fileItem, response, status, headers) {
                uploader.removeFromQueue(fileItem);
            };

            ctrl.uploader.onCancelItem = function(fileItem, response, status, headers) {
                uploader.removeFromQueue(fileItem);
            };
            
            loadFiles();
            
            //the /card-data has various card data related event that are pushed from the server that we must react
            onDestroyStomp = StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function(e) {
                var type = JSON.parse(e.body).type;
                if(type.match(/FILE$/g)) {
                    loadFiles();
                }
            });
        };
        
        ctrl.$onDestroy = function onDestroy() {
        	onDestroyStomp();
        };
        

        // -----

        function loadFiles() {
            Card.files(ctrl.card.id).then(function(files) {
                ctrl.files = files;
            });
        };
    };
})();
