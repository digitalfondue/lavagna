(function() {
	
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
		}
        
		function cancel() {
			$mdDialog.hide();
		}

        function createCard(name, columnId) {
            Board.createCardFromTop(ctrl.boardShortName, columnId, {name: name}).then(function() {
            	ctrl.name = null;
            	ctrl.dialogNewCardForm.$setPristine();
            	ctrl.dialogNewCardForm.$setUntouched();//clear up error messages
            }).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.board.create-card.error'}, false);
            });
        };
	}
		
})();
