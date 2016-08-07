(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFile', {
        bindings: {
            card: '<',
            file: '<'
        },
        controller: CardFileController,
        templateUrl: 'app/components/card/file/card-file.html'
    });

    function CardFileController(Card, Notification) {
        var ctrl = this;

        ctrl.delete = function() {
            Card.deleteFile(ctrl.file.cardDataId).then(function(event) {
                Notification.addNotification('success', {key : 'notification.card.FILE_DELETE.success'}, true, true, function(notification) {
                    Card.undoDeleteFile(event.id).then(notification.acknowledge);
                });
            }, function(error) {
                Notification.addAutoAckNotification('error', {key : 'notification.card.FILE_DELETE.error'}, false);
            });
        };
    };
})();
