(function() {
	angular
		.module('lavagna.components')
		.component('lvgDialogNewColumn', {
			templateUrl: 'app/components/dialog-new-column/dialog-new-column.html',
			bindings: {
				boardName: '<',
				columnsDefinition: '<'
			},
			controller: function($mdDialog, Board, Notification) {
				var ctrl = this;

				ctrl.cancel = function() {
					$mdDialog.hide();
				}

				ctrl.createColumn = function(columnToCreate) {
		            Board.createColumn(ctrl.boardName, columnToCreate).then(function() {
		                columnToCreate.name = null;
		                columnToCreate.definition = null;
		                $mdDialog.hide();
		            }).catch(function(error) {
		                Notification.addAutoAckNotification('error', { key : 'notification.board.create-column.error'}, false);
		            });
		        };
			}
		})

})();
