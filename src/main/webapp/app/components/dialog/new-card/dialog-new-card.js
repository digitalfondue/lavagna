(function () {
    'use strict';

    angular
        .module('lavagna.components')
        .component('lvgDialogNewCard', {
            templateUrl: 'app/components/dialog/new-card/dialog-new-card.html',
            bindings: {
                boardShortName: '<',
                columns: '<',
                column: '<'
            },
            controller: ['$mdDialog', 'Board', 'Notification', DialogNewCardController]
        });

    function DialogNewCardController($mdDialog, Board, Notification) {
        var ctrl = this;

        ctrl.cancel = cancel;
        ctrl.createCard = createCard;


        ctrl.$onInit = function init() {
            ctrl.columnId = ctrl.column.id;
            ctrl.processing = false;
        };

        function cancel() {
            $mdDialog.hide();
        }

        function createCard(name, columnId) {
            ctrl.processing = true;
            Board.createCardFromTop(columnId, {name: name}).then(function () {
                ctrl.name = null;
                ctrl.dialogNewCardForm.$setPristine();
                ctrl.dialogNewCardForm.$setUntouched();// clear up error messages
            }, function () {
                Notification.addAutoAckNotification('error', { key: 'notification.board.create-card.error'}, false);
            }).finally(function () {
                ctrl.processing = false;
            });
        }
    }
}());
