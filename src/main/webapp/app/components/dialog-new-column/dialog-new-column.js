(function() {
	
	
	angular
		.module('lavagna.components')
		.component('lvgDialogNewColumn', {
			templateUrl: 'app/components/dialog-new-column/dialog-new-column.html',
			bindings: {
				dialogTitle: '<',
				action: '=',
				projectName: '<',
				boardName: '<'
			},
			controller: function($mdDialog, Board, Project, Notification) {
				var ctrl = this;
				
				ctrl.cancel = function() {
					$mdDialog.hide();
				}
				
				var projectName = ctrl.projectName;
				var boardName = ctrl.boardName;
				
				
				Project.columnsDefinition(projectName).then(function(definitions) {
		            ctrl.columnsDefinition = definitions;
		        });
				
				ctrl.createColumn = function(columnToCreate) {
		            Board.createColumn(boardName, columnToCreate).then(function() {
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