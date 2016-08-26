(function () {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Menu', {
		controller: ['BulkOperations', 'Card', 'Board', '$filter', lvgCardFragmentV2MenuCtrl],
		templateUrl: 'app/components/card-fragment-v2/menu/card-fragment-v2-menu.html',
		bindings: {
			mdPanelRef:'<',
			isSelfWatching : '<',
	    	isAssignedToCard: '<',
	    	currentUserId: '<',
	    	card:'<'
		}
	});
	
	
	function lvgCardFragmentV2MenuCtrl(BulkOperations, Card, Board, $filter) {
		var ctrl = this;
		
		ctrl.$onInit = function lvgCardFragmentV2MenuCtrlOnInit() {
			ctrl.moveColumnFlag = false;
			ctrl.cloneCardFlag = false;
			loadColumns();
		}
		
		
		ctrl.close = close;
		ctrl.watchCard = watchCard;
		ctrl.unWatchCard = unWatchCard;
		ctrl.assignToCurrentUser = assignToCurrentUser;
		ctrl.removeAssignForCurrentUser = removeAssignForCurrentUser;
		ctrl.moveCard = moveCard;
		ctrl.moveToColumn = moveToColumn;
		
		
		var cardByProject = {};
		cardByProject[ctrl.card.projectShortName] = [ctrl.card.id];
		var currentUserId = {id: ctrl.currentUserId};
		
		
		function watchCard() {
            BulkOperations.watch(cardByProject, currentUserId);
		}
	
		function unWatchCard() {
            BulkOperations.unWatch(cardByProject, currentUserId);
		}
		
		function assignToCurrentUser() {
            BulkOperations.assign(cardByProject, currentUserId);
		}
		
		function removeAssignForCurrentUser() {
            BulkOperations.removeAssign(cardByProject, currentUserId);
		}
		
		function moveCard(location) {
			Card.moveAllFromColumnToLocation(ctrl.card.columnId, [ctrl.card.id], location);
		}
		
		function loadColumns() {
			Board.columnsByLocation(ctrl.card.boardShortName, 'BOARD').then(function(columns) {
            	ctrl.moveColumns = $filter('filter')(columns, function(col) {return col.id != ctrl.card.columnId});
            });
		}
		
		function moveToColumn(toColumn) {
			Card.findByColumn(toColumn.id).then(function(cards) {
				var ids = [];
				for (var i = 0;i<cards.length;i++) {
					ids.push(cards[i].id);
				}
				ids.push(ctrl.card.id);
				return ids;
			}).then(function(ids) {
				Board.moveCardToColumn(ctrl.card.id, ctrl.card.columnId, toColumn.id, {newContainer: ids});
			}).then(close)
		}
		
		
		function close() {
			ctrl.mdPanelRef.close();
		}
		
	};
	
})();