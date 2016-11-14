(function () {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Menu', {
		controller: ['BulkOperations', 'Card', 'Board', 'Notification', 'Project', '$filter', CardFragmentV2MenuController],
		templateUrl: 'app/components/card-fragment-v2/menu/card-fragment-v2-menu.html',
		bindings: {
			mdPanelRef:'<',
			isSelfWatching : '<',
	    	isAssignedToCard: '<',
	    	currentUserId: '<',
	    	card:'<'
		}
	});


	function CardFragmentV2MenuController(BulkOperations, Card, Board, Notification, Project, $filter) {
		var ctrl = this;

		ctrl.close = close;
		ctrl.watchCard = watchCard;
		ctrl.unWatchCard = unWatchCard;
		ctrl.assignToCurrentUser = assignToCurrentUser;
		ctrl.removeAssignForCurrentUser = removeAssignForCurrentUser;
		ctrl.moveCard = moveCard;
		ctrl.moveToColumn = moveToColumn;
		ctrl.cloneCard = cloneCard;
		ctrl.handleKey = handleKey;

		//
		ctrl.$onInit = function init() {
			ctrl.moveColumnFlag = false;
			ctrl.cloneCardFlag = false;
			loadColumns();
			loadAllProjectColumns();
		}
		//


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

		function loadAllProjectColumns() {
			Project.findAllColumns(ctrl.card.projectShortName).then(function(columns) {
                ctrl.projectColumns = columns;
                var cols = [[]];
                var orderedColumns = $filter('orderBy')(columns, ['board','columnName']);
                for(var i = 0; i < orderedColumns.length;i++) {
                	var col = orderedColumns[i];
                	var latestSegment = cols.length-1;
                	if(cols[latestSegment] && cols[latestSegment][0] && cols[latestSegment][0].board !== col.board) {
                		cols.push([]);
                	}
                	cols[cols.length-1].push(col);
                }
                ctrl.columnsByProject = cols;
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


		function cloneCard(clonetoColumn) {
            Card.clone(ctrl.card.id, clonetoColumn.columnId).then(function() {
                Notification.addAutoAckNotification('success', { key : 'notification.card.clone.success'}, false);
            }).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.card.clone.error'}, false);
            }).then(close);
        }

		function close() {
			ctrl.mdPanelRef.close();
		}

		//navigate up/down in the menu
		function handleKey(event) {
			if(event.keyCode === 40 || event.keyCode === 38) {
				var currentNode = document.activeElement.parentNode;
				var attrName = event.keyCode === 40 ? 'nextElementSibling' : 'previousElementSibling';
				while(currentNode[attrName] != null) {
					currentNode = currentNode[attrName];
					if(!currentNode.classList.contains('lavagna-hide')) {
						break;
					}
				}
				currentNode.children[0].focus();
			}
		}
	};

})();
