(function() {

	angular
		.module('lavagna.components')
		.component('lvgDialogNewCard', {
			templateUrl: 'app/components/dialog/new-card/dialog-new-card.html',
			bindings: {
				boardShortName: '<',
				columns: '<',
				column: '<'
			},
			controller: function($mdDialog, Board, Notification) {
				var ctrl = this;

                ctrl.columnId = ctrl.column.id;

				ctrl.cancel = function() {
					$mdDialog.hide();
				}

		        ctrl.createCard = function(name, columnId) {
		            Board.createCardFromTop(ctrl.boardShortName, columnId, {name: name}).then(function() {
                        name = null;
                    }).catch(function(error) {
                        Notification.addAutoAckNotification('error', { key : 'notification.board.create-card.error'}, false);
                    });
		        };
			}
		})

})();
