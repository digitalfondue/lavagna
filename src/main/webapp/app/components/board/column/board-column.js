(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgBoardColumn', BoardColumnComponent);

    function BoardColumnComponent() {
        return {
            restrict: 'E',
            controller: BoardColumnController,
            controllerAs: 'boardColumnCtrl',
            scope: true,
            bindToController: {
                projectShortName: '@',
                metadataRef: '&',
                boardShortName:'@',
                column: '=',
                selectedCards: '=',
                searchFilterRef: '&',
                userRef:'&'
            },
            templateUrl: 'app/components/board/column/board-column.html'
        }
    }

    function BoardColumnController($scope, $filter, $mdDialog, Board, Card, Label, Notification, StompClient, BulkOperations) {
        var ctrl = this;
        
        ctrl.user = ctrl.userRef();
        ctrl.searchFilter = ctrl.searchFilterRef();
        
        //
        var r = [];
        ctrl.getMetadataHash = function getMetadataHash() {
        	var hash = '';
        	var metadata = ctrl.metadataRef();
        	if(metadata) {
        		hash = metadata.hash;
        	}
        	r[0] = hash;
        	return r;
        }
        //
        
        ctrl.movedCard = function(card) {
        	//
    		for(var i = 0; i < ctrl.cardsInColumn.length; i++) {
        		if(ctrl.cardsInColumn[i].id == card.id) {
        			ctrl.cardsInColumn.splice(i, 1);
        			break;
        		}
        	}
        }
        
        ctrl.dropCard = function(index, card) {
        	
        	//remove card before dropping if it's in the same column...
        	if(card.columnId === ctrl.column.id) {
        		for(var i = 0; i < ctrl.cardsInColumn.length; i++) {
            		if(ctrl.cardsInColumn[i].id == card.id) {
            			ctrl.cardsInColumn.splice(i, 1);
            			break;
            		}
            	}
        	}
        	return card;
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

        ctrl.createCardFromTop = function(cardToCreateFromTop) {
            Board.createCardFromTop(boardShortName, ctrl.column.id, {name: cardToCreateFromTop.name}).then(function() {
                cardToCreateFromTop.name = null;
            }).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.board.create-card.error'}, false);
            });
        };

        ctrl.createCard = function(cardToCreate) {
            Board.createCard(boardShortName, ctrl.column.id, {name: cardToCreate.name}).then(function() {
                cardToCreate.name = null;
            }).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.board.create-card.error'}, false);
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

            $mdDialog.show({
                templateUrl: 'app/components/board/column/confirm-modal-fragment.html',
                controller: function($scope) {
                    $scope.title = $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-column-to-location.title');
                    $scope.operation = $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-column-to-location', {columnName: ctrl.column.name, location: $filter('capitalize')(location)});
                    $scope.confirm = function() {
                        confirmAction();
                        $mdDialog.hide();
                    };
                    $scope.cancel = function() {
                    	$mdDialog.hide();
                    }
                }
            });


        };

        ctrl.moveAllCardsInColumn = function (cards, location) {

            var cardIds = cards.map(function(c) {return c.id});
            var confirmAction = function() {Card.moveAllFromColumnToLocation(ctrl.column.id, cardIds, location).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
            });};

            $mdDialog.show({
                templateUrl: 'app/components/board/column/confirm-modal-fragment.html',
                controller: function($scope) {
                    $scope.title =  $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-card-from-column-to-location.title');
                    $scope.operation = $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-card-from-column-to-location',
                        {columnName: ctrl.column.name, location: $filter('capitalize')(location)});

                    $scope.confirm = function() {
                        confirmAction();
                        $mdDialog.hide();
                    };
                    
                    $scope.cancel = function() {
                    	$mdDialog.hide();
                    }
                }
            });
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
