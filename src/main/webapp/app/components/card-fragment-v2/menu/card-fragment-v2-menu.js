(function () {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Menu', {
		controller: ['BulkOperations', 'Card', lvgCardFragmentV2MenuCtrl],
		templateUrl: 'app/components/card-fragment-v2/menu/card-fragment-v2-menu.html',
		bindings: {
			mdPanelRef:'<',
			isSelfWatching : '<',
	    	isAssignedToCard: '<',
	    	currentUserId: '<',
	    	card:'<'
		}
	});
	
	
	function lvgCardFragmentV2MenuCtrl(BulkOperations, Card) {
		var ctrl = this;
		
		ctrl.close = close;
		ctrl.watchCard = watchCard;
		ctrl.unWatchCard = unWatchCard;
		ctrl.assignToCurrentUser = assignToCurrentUser;
		ctrl.removeAssignForCurrentUser = removeAssignForCurrentUser;
		ctrl.moveCard = moveCard;
		
		
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
		};
		
		
		function close() {
			ctrl.mdPanelRef.close();
		}
		
	};
	
})();