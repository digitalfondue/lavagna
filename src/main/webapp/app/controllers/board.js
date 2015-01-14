(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ColumnCtrl', function($stateParams, $scope, $filter, $modal, Card, Label, Notification, StompClient) {

		$scope.initializeColumnCtrl = function(columnId) {

			var loadCards = function() {
				Card.findByColumn(columnId).then(function(res) {
					$("[data-lvg-column-id="+columnId+"] .lavagna-to-be-cleaned-up").remove();
					$scope.cardsInColumn = res;
					$scope.loaded = true;
					$scope.foundCards[columnId] = res;
				});
			};
			StompClient.subscribe($scope, '/event/column/'+columnId+'/card', loadCards);

			$scope.$on('loadcards', loadCards);

			loadCards();

			$scope.$on('$destroy', function() {
				delete $scope.foundCards[columnId];
			});
		};

		$scope.selectAllInColumn = function() {
			angular.forEach($filter('filter')($scope.cardsInColumn, $scope.cardFilter), function(c) {
				$scope.selectedCards[c.id] = true;
			})
		};
		$scope.unSelectAllInColumn = function() {
			angular.forEach($filter('filter')($scope.cardsInColumn, $scope.cardFilter), function(c) {
				delete $scope.selectedCards[c.id];
			});
		};
		$scope.$on('selectall', $scope.selectAllInColumn);
		$scope.$on('unselectall', $scope.unSelectAllInColumn);

		$scope.addUserLabelValue = function(cardId, labelId, user) {
        	Label.addValueToCard(cardId, labelId, Label.userVal(user)).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
        	});
        };

        $scope.removeLabelValueForId = function(cardId, cardLabels, labelId) {
            for(var i = 0; i < cardLabels.length; i++) {
                if(cardLabels[i].labelId == labelId) {
                    Label.removeValue(cardId, cardLabels[i].labelValueId).catch(function(error) {
                		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
                	});
                    break;
                }
            }
        };

        $scope.moveAllCardsInColumn = function (col, cards, location) {

        	var cardIds = cards.map(function(c) {return c.id});
	    	var confirmAction = function() {Card.moveAllFromColumnToLocation(col.id, cardIds, location).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
        	});};

	        $modal.open({
	        	templateUrl: 'partials/fragments/confirm-modal-fragment.html',
	        	controller: function($scope) {
	        		$scope.title =  $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-card-from-column-to-location.title');
	        		$scope.operation = $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-card-from-column-to-location', {columnName: col.name, location: $filter('capitalize')(location)});

	        		$scope.confirm = function() {
	        			confirmAction();
	        			$scope.$close();
	        		};
	        	},
	        	size: 'md',
	        	windowClass: 'lavagna-modal'
	        });
        }
        
        // dependencies for card fragment
        $scope.cardFragmentDependencies = {};
        var cardFragmentDependenciesToCopy = ['labelNameToId', 'addUserLabelValue',
		                      'removeLabelValueForId', 'moveCard', 'currentUserId', 'columns'];
		for(var k in cardFragmentDependenciesToCopy) {
			$scope.cardFragmentDependencies[cardFragmentDependenciesToCopy[k]] = $scope[cardFragmentDependenciesToCopy[k]];
		}

	});

	module.controller('BoardCtrl', function($rootScope, $stateParams, $scope, $location, $filter, $log, $timeout, $http, $modal,
			Board, Card, Project, LabelCache, Label, Search, StompClient, User, Notification,//
			project, board //resolved by ui-router
			) {

		$scope.project = project;
		$scope.board = board;

		var boardName = $stateParams.shortName;
		$scope.projectName = $stateParams.projectName;
		var projectName = $stateParams.projectName;


		//TODO check redundancy
		$scope.boardName = boardName;
		$scope.boardShortName = boardName;



		User.currentCachedUser().then(function(currentUser) {
			$scope.currentUserId = currentUser.id;
		});

		$scope.moveCard = function(card, location) {
			Card.moveAllFromColumnToLocation(card.columnId, [card.id], location);
		};

		$scope.backFromLocation = function() {
			$scope.locationOpened=false;
			$scope.sideBarLocation=undefined;
		};

        $scope.labelNameToId = {};
		var loadLabel = function() {
			LabelCache.findByProjectShortName(projectName).then(function(labels) {
				$scope.labelNameToId = {};
                for(var k in labels) {
                    $scope.labelNameToId[labels[k].name] = k;
                }
			});
		};
		loadLabel();


		var unbind = $rootScope.$on('refreshLabelCache-' + projectName, function() {
			loadLabel();
			$scope.$broadcast('loadcards');
		});
		$scope.$on('$destroy', unbind);

		//keep track of the selected cards
		$scope.selectedCards = {};
		$scope.foundCards = {};
		
		$scope.editMode = false;
		$scope.switchEditMode = function() {
			$scope.editMode = !$scope.editMode;
		};


		$scope.selectAll = function() {
			$scope.$broadcast('selectall');
		};

		$scope.unSelectAll = function() {
			$scope.$broadcast('unselectall');
		};

		var selectedVisibleCardsId = function() {
			var ids = [];
			for(var columnId in $scope.foundCards) {
				if($scope.foundCards[columnId]) {
					angular.forEach($filter('filter')($scope.foundCards[columnId], $scope.cardFilter), function(c) {
						if($scope.selectedCards[c.id]) {
							ids.push(c.id);
						}
					});
				}
			}
			return ids;
		};

		var selectedVisibleCardsIdByColumnId = function() {
			var res = {};
			for(var columnId in $scope.foundCards) {
				if($scope.foundCards[columnId]) {
					angular.forEach($filter('filter')($scope.foundCards[columnId], $scope.cardFilter), function(c) {
						if($scope.selectedCards[c.id]) {
							if(!res[c.columnId]) {
								res[c.columnId] = [];
							}
							res[c.columnId].push(c.id);
						}
					});
				}
			}
			return res;
		};

		$scope.selectedVisibleCount = function() {
			return selectedVisibleCardsId().length;
		};


		$scope.$on('refreshSearch', function(ev, searchFilter) {
			User.currentCachedUser().then(function(user) {
				try {
					Search.buildSearchFilter(searchFilter.searchFilter, $scope.columns, user.id).then(function(filterFun) {
						$scope.cardFilter = filterFun;
						$timeout(function() {
							$location.search(searchFilter.location);
						});
					});

				} catch(e) {
					$log.debug('parsing exception', e);
				}
			});
		});
		//

		//-----------------------------------------------------------------------------------------------------
		//--- TODO cleanup

		//

		Project.columnsDefinition(projectName).then(function(definitions) {
			$scope.columnsDefinition = definitions;
		});
		//

		$scope.hasMetadata = function(card) {
			if(card.counts == null)
				return false; //empty
			return card.counts['COMMENT'] != undefined || card.counts['FILE'] != undefined
				|| card.counts['ACTION_CHECKED'] != undefined || card.counts['ACTION_UNCHECKED'] != undefined;
		};


		$scope.isEmpty = function(obj) {
			return Object.keys(obj).length === 0;
		};

		//----------
		$scope.columnsLocation = 'BOARD';

		var assignToColumn = function(columns) {
			$(".lavagna-to-be-cleaned-up").remove();
			$scope.columns = columns;
			$rootScope.$broadcast('requestSearch');
		};

		StompClient.subscribe($scope, '/event/board/'+boardName+'/location/BOARD/column', function() {
			Board.columns(boardName, $scope.columnsLocation).then(assignToColumn);
		});

		Board.columns(boardName, 'BOARD').then(assignToColumn);

		//-------------

		User.hasPermission('MOVE_COLUMN', $stateParams.projectName).then(function() {
			$scope.sortableColumnOptions = {
					tolerance: "pointer",
					handle: '.lvg-column-handle',
					forceHelperSize: true,
					items: '> .lavagna-sortable-board-column',
					cancel: 'input,textarea,button,select,option,.lavagna-not-sortable-board-column,.lvg-board-column-menu',
					placeholder: "lavagna-column-placeholder",
					start : function(e, ui) {
						ui.placeholder.height(ui.helper.outerHeight());
					},
					stop: function(e, ui) {
						if(ui.item.data('hasUpdate')) {
							var colPos =  ui.item.parent().sortable("toArray", {attribute: 'data-lvg-column-id'}).map(function(i) {return parseInt(i, 10);});
							//mark the placeholder item
							//ui.item.addClass('lavagna-to-be-cleaned-up');
							Board.reorderColumn(boardName, $scope.columnsLocation, colPos).catch(function(error) {
				        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
				        	});
						}
						ui.item.removeData('hasUpdate');
					},
					update : function(e, ui) {
						ui.item.data('hasUpdate', true);
					}
			};
		}, function() {
			$scope.sortableColumnOptions = false;
		});


		User.hasPermission('MOVE_CARD', $stateParams.projectName).then(function() {
			$scope.sortableCardOptions = {
					tolerance: "pointer",
					connectWith: ".lavagna-board-cards,.sidebar-dropzone",
					cancel: '.lvg-not-sortable-card',
					placeholder: "lavagna-card-placeholder",
					start : function(e, ui) {
						$("#sidebar-drop-zone").show();
						ui.placeholder.height(ui.helper.outerHeight());
						ui.item.data('initialColumnId', ui.item.parent().parent().parent().attr('data-lvg-column-id'));
					},
					stop: function(e, ui) {
						$("#sidebar-drop-zone").hide();
						if(ui.item.data('hasUpdate')) {
							var cardId = parseInt(ui.item.attr('data-lvg-card-id'), 10);
							var oldColumnId = parseInt(ui.item.data('initialColumnId'), 10);
							var newColumnId = parseInt(ui.item.data('newColumnId'), 10);
							var ids = ui.item.parent().sortable("toArray", {attribute: 'data-lvg-card-id'}).map(function(i) {return parseInt(i, 10);});

							ui.item.addClass('lavagna-to-be-cleaned-up');
							ui.item.replaceWith(ui.item.clone());

							if(oldColumnId === newColumnId) {
								Board.updateCardOrder(boardName, oldColumnId, ids).catch(function(error) {
					        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
					        	});
							} else {
								Board.moveCardToColumn(cardId, oldColumnId, newColumnId, {newContainer: ids}).catch(function(error) {
					        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
					        	});
							}
						}
						ui.item.removeData('hasUpdate');
						ui.item.removeData('initialColumnId');
						ui.item.removeData('newColumnId');
					},
					update : function(e, ui) {
						ui.item.data('newColumnId', ui.item.parent().parent().parent().attr('data-lvg-column-id'));
						ui.item.data('hasUpdate', true);
					},
					//http://stackoverflow.com/a/20228500
					over: function(e, ui) {
						ui.item.data('sortableItem').scrollParent = ui.placeholder.parent();
					    ui.item.data('sortableItem').overflowOffset = ui.placeholder.parent().offset();
					}
			};
		}, function() {
			$scope.sortableCardOptions = false;
		});




		//will be used as a map columnState[columnId].editColumnName = true/false
		$scope.columnState = {};

		$scope.createColumn = function(columnToCreate) {
			Board.createColumn(boardName, columnToCreate).then(function() {
				columnToCreate.name = null;
				columnToCreate.definition = null;
			}).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.board.create-column.error'}, false);
        	});
		};

		$scope.createCardFromTop = function(cardToCreateFromTop, column) {
			Board.createCardFromTop(boardName, column.id, {name: cardToCreateFromTop.name}).then(function() {
				cardToCreateFromTop.name = null;
			}).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.board.create-card.error'}, false);
        	});
		};

		$scope.createCard = function(cardToCreate, column) {
			Board.createCard(boardName, column.id, {name: cardToCreate.name}).then(function() {
				cardToCreate.name = null;
			}).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.board.create-card.error'}, false);
        	});
		};

		$scope.saveNewColumnName = function(newName, column) {
			Board.renameColumn(boardName, column.id, newName).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.board.rename-column.error'}, false);
        	});
		};

		$scope.setColumnDefinition = function(definition, column) {
			Board.redefineColumn(boardName, column.id, definition).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.board.redefine-column.error'}, false);
        	});
		};

	    $scope.moveColumn = function(column, location) {

	    	var confirmAction = function() {Board.moveColumnToLocation(column.id, location).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
        	});};

	    	$modal.open({
	        	templateUrl: 'partials/fragments/confirm-modal-fragment.html',
	        	controller: function($scope) {
	        		$scope.title = $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-column-to-location.title');
	        		$scope.operation = $filter('translate')('partials.fragments.confirm-modal-fragment.operation.move-column-to-location', {columnName: column.name, location: $filter('capitalize')(location)});
	        		$scope.confirm = function() {
	        			confirmAction();
	        			$scope.$close();
	        		};
	        	},
	        	size: 'md',
	        	windowClass: 'lavagna-modal'
	        });


	    };
		//-----------------------------------------------------------------------------------------------------

	    var formatBulkRequest = function() {
	    	var r = {};
	    	r[projectName] = selectedVisibleCardsId();
	    	return r;
	    };
	    
	    $scope.formatBulkRequest = formatBulkRequest;
	    $scope.selectedVisibleCardsIdByColumnId = selectedVisibleCardsIdByColumnId;

	    //-----------------------------------------------------------------------------------------------------
	    
	    //some sidebar controls
	    $scope.sideBarLocation = undefined;
	    $scope.toggledSidebar = false;
	    
	    $scope.sidebarHandler = function(location) {
	    	 $scope.sideBarLocation = location;
	    	 
	    	// shall we close the sidebar?
			if(location === 'NONE' && $scope.toggledSidebar) {
				$scope.toggledSidebar = false;
			};
				
			// shall we open the sidebar?
			if(location !== 'NONE' && !$scope.toggledSidebar) {
				$scope.toggledSidebar = true;
			};
	    };
	});
})();