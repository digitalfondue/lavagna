(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardColumn', {
    	controller: BoardColumnController,
        bindings: {
            projectShortName: '@',
            metadataRef: '&',
            boardShortName:'@',
            column: '=',
            selectedCards: '=',
            searchFilterRef: '&',
            userRef:'&'
        },
        templateUrl: 'app/components/board/column/board-column.html'
    });

    function BoardColumnController($scope, $filter, $mdDialog, $element, $translate, Project, Board, Card, Label, Notification, StompClient, BulkOperations, SharedBoardDataService) {
        var ctrl = this;

        ctrl.user = ctrl.userRef();
        ctrl.searchFilter = ctrl.searchFilterRef();

        //
        ctrl.metadata = ctrl.metadataRef();

        //
        ctrl.dragStartCard = function(item) {
        	SharedBoardDataService.startDrag();
        	SharedBoardDataService.dndColumnOrigin = ctrl;
        	SharedBoardDataService.dndCardOrigin = item;
        }

        ctrl.dragEndCard = function(item) {
        	SharedBoardDataService.endDrag();
        }
        //

        ctrl.removeCard = function(card) {
        	for(var i = 0; i < ctrl.cardsInColumn.length; i++) {
        		if(ctrl.cardsInColumn[i].id === card.id) {
        			ctrl.cardsInColumn.splice(i, 1);
        			break;
        		}
        	}
        }

        ctrl.dropCard = function dropCard(index) {
        	var card = SharedBoardDataService.dndCardOrigin;
        	SharedBoardDataService.dndCardOrigin = null;
        	if(!card) {
        		return;
        	}
        	//ignore drop as it's the same position
        	if(card.columnId === ctrl.column.id && ctrl.cardsInColumn[index] && ctrl.cardsInColumn[index].id == card.id) {
        		return;
        	}

        	// remove card from origin column
        	if(SharedBoardDataService.dndColumnOrigin) {
        		SharedBoardDataService.dndColumnOrigin.removeCard(card);
        		SharedBoardDataService.dndColumnOrigin = null;
        	}

        	// insert card at correct index
        	ctrl.cardsInColumn.splice(index, 0, card);
        	//

        	var oldColumnId = card.columnId;
        	var newColumnId = ctrl.column.id;
        	var cardId = card.id;
        	var ids = [];

        	angular.forEach(ctrl.cardsInColumn, function(card) {
        		ids.push(card.id);
        	});

        	if(oldColumnId === newColumnId) {
        		//internal reorder
                Board.updateCardOrder(ctrl.boardShortName, oldColumnId, ids).catch(function(error) {
                    Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
                });
            } else {
            	//move card from one column to another
                Board.moveCardToColumn(cardId, oldColumnId, newColumnId, {newContainer: ids}).catch(function(error) {
                    Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
                });
            }
        }

        //
        var initializeColumn = function() {

            var columnId = ctrl.column.id;

            var loadCards = function() {
                Card.findByColumn(columnId).then(function(res) {
                	res.columnId = columnId;
                	ctrl.cardsInColumn = res;
                	ctrl.loaded = true;

                	// sync selection, in case of a moved selected card
                	// not optimal, but it should be good enough
                	if(ctrl.selectedCards[ctrl.column.id]) {
                		for(var key in ctrl.selectedCards[ctrl.column.id]) {
                			if(ctrl.selectedCards[ctrl.column.id][key] && !idExist(parseInt(key))) {
                				delete ctrl.selectedCards[ctrl.column.id][key];
                			}
                		}
                	}


                	function idExist(id) {
                		for(var i = 0; i < ctrl.cardsInColumn.length;i++) {
                			if(ctrl.cardsInColumn[i].id === id) {
                				return true;
                			}
                		}
                		return false;
                	}

                	//
                });
            };
            StompClient.subscribe($scope, '/event/column/'+columnId+'/card', loadCards);

            $scope.$on('loadcards', loadCards);

            loadCards();
        };

        initializeColumn();

        //


        var boardShortName = ctrl.boardShortName;
        var projectShortName = ctrl.projectShortName;

        //capture all status variables
        ctrl.columnState = {};



        ctrl.selectAllInColumn = function() {
            angular.forEach($filter('filter')(ctrl.cardsInColumn, ctrl.searchFilter.cardFilter), function(c) {
            	if(!ctrl.selectedCards[ctrl.column.id]) {
            		ctrl.selectedCards[ctrl.column.id] = {};
            	}
                ctrl.selectedCards[ctrl.column.id][c.id] = true;
            });
            $scope.$broadcast('updatecheckbox');
        };
        ctrl.unSelectAllInColumn = function() {
            angular.forEach($filter('filter')(ctrl.cardsInColumn, ctrl.searchFilter.cardFilter), function(c) {
                delete ctrl.selectedCards[ctrl.column.id];
            });
            $scope.$broadcast('updatecheckbox');
        };


        $scope.$on('selectall', ctrl.selectAllInColumn);
        $scope.$on('unselectall', ctrl.unSelectAllInColumn);

        ctrl.newCard = function() {
            $mdDialog.show({
                template: '<lvg-dialog-new-card board-short-name="vm.boardShortName" columns="vm.columns" column="vm.column"></lvg-dialog-new-card>',
                locals: {
                    column: ctrl.column,
                    boardShortName: boardShortName
                },
                bindToController: true,
                resolve: {
                    columns: function() {
                        return Board.columnsByLocation(boardShortName, 'BOARD');
                    }
                },
                controller: function(columns) {
                    this.columns = columns;
                },
                controllerAs: 'vm'
            });
        };

        ctrl.assignToCurrentUser = function(cardId, currentUserId) {
            var cardByProject = {};
            cardByProject[projectShortName] = [cardId];
            BulkOperations.assign(cardByProject, {id: currentUserId});
        };

        ctrl.removeAssignForCurrentUser = function(cardId, currentUserId) {
            var cardByProject = {};
            cardByProject[projectShortName] = [cardId];
            BulkOperations.removeAssign(cardByProject, {id: currentUserId});
        };

        ctrl.watchCard = function(cardId, currentUserId) {
            var cardByProject = {};
            cardByProject[projectShortName] = [cardId];
            BulkOperations.watch(cardByProject, {id: currentUserId});
        };

        ctrl.unWatchCard = function(cardId, currentUserId) {
            var cardByProject = {};
            cardByProject[projectShortName] = [cardId];
            BulkOperations.unWatch(cardByProject, {id: currentUserId});
        };

        ctrl.moveColumn = function(location) {
            var confirmAction = function() {Board.moveColumnToLocation(ctrl.column.id, location).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
            });};
            
            
            var title = $translate.instant('partials.fragments.confirm-modal-fragment.operation.move-column-to-location', {columnName: ctrl.column.name, location: $filter('capitalize')(location)});
			var confirm = $mdDialog.confirm()
				.title(title)
				.ariaLabel(title)
				.ok($translate.instant('button.yes'))
				.cancel($translate.instant('button.no'));

			$mdDialog.show(confirm).then(function() {
				confirmAction();
			}, function() {});
        };

        ctrl.moveAllCardsInColumn = function (cards, location) {

            var cardIds = cards.map(function(c) {return c.id});
            var confirmAction = function() {Card.moveAllFromColumnToLocation(ctrl.column.id, cardIds, location).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
            });};
            
            var title = $translate.instant('partials.fragments.confirm-modal-fragment.operation.move-card-from-column-to-location', {columnName: ctrl.column.name, location: $filter('capitalize')(location)});
			var confirm = $mdDialog.confirm()
				.title(title)
				.ariaLabel(title)
				.ok($translate.instant('button.yes'))
				.cancel($translate.instant('button.no'));

			$mdDialog.show(confirm).then(function() {
				confirmAction();
			}, function() {});
        }

        ctrl.saveNewColumnName = function(newName) {
            Board.renameColumn(boardShortName, ctrl.column.id, newName).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.board.rename-column.error'}, false);
            });
        };

        ctrl.setColumnDefinition = function(definition) {
            Board.redefineColumn(boardShortName, ctrl.column.id, definition).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.board.redefine-column.error'}, false);
            });
        };
    }
})();
