(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentBoard', {
        controller: BoardController,
        controllerAs: 'boardCtrl',
        bindings: {
            project: '=',
            board: '='
        },
        templateUrl: 'app/components/board/board/board.html'
    });

    function BoardController($rootScope, $scope, $location, $filter, $log, $timeout,
        Board, Card, Project, LabelCache, Search, StompClient, User, Notification) {

        var ctrl = this;
        ctrl.searchFilter = {};

        var boardName = ctrl.board.shortName;
        var projectName = ctrl.project.shortName;
        

        User.currentCachedUser().then(function(currentUser) {
            ctrl.currentUserId = currentUser.id;
        });

        ctrl.moveCard = function(card, location) {
            Card.moveAllFromColumnToLocation(card.columnId, [card.id], location);
        };

        ctrl.backFromLocation = function() {
            ctrl.locationOpened=false;
            ctrl.sideBarLocation=undefined;
        };

        ctrl.labelNameToId = {};
        var loadLabel = function() {
            LabelCache.findByProjectShortName(projectName).then(function(labels) {
                ctrl.labelNameToId = {};
                for(var k in labels) {
                    ctrl.labelNameToId[labels[k].name] = k;
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
        ctrl.selectedCards = {};
        //ctrl.foundCards = {};

        ctrl.editMode = false;
        ctrl.switchEditMode = function() {
            ctrl.editMode = !ctrl.editMode;
        };


        ctrl.selectAll = function() {
            $scope.$broadcast('selectall');
        };

        ctrl.unSelectAll = function() {
            $scope.$broadcast('unselectall');
        };

        var selectedVisibleCardsId = function() {
            var ids = [];
            
            angular.forEach(selectedVisibleCardsIdByColumnId(), function(val) {
            	ids = ids.concat(val);
            });
            
            return ids;
        };

        var selectedVisibleCardsIdByColumnId = function() {
            var res = {};
            angular.forEach(ctrl.selectedCards, function(column, columnId) {
            	angular.forEach(column, function(isSelected, cardId) {
            		if(isSelected) {
	            		if(!res[columnId]) {
	            			res[columnId] = [];
	            		}
	            		res[columnId].push(parseInt(cardId,10));
            		}
            		
            	})
            })
            return res;
        };

        ctrl.selectedVisibleCount = function() {
        	return selectedVisibleCardsId().length;
        };


        $scope.$on('refreshSearch', function(ev, searchFilter) {
            User.currentCachedUser().then(function(user) {
                try {
                    Search.buildSearchFilter(searchFilter.searchFilter, ctrl.columns, user.id).then(function(filterFun) {
                        ctrl.searchFilter.cardFilter = filterFun;
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
            ctrl.columnsDefinition = definitions;
        });
        //

        ctrl.hasMetadata = function(card) {
            if(card.counts == null)
                return false; //empty
            return card.counts['COMMENT'] != undefined || card.counts['FILE'] != undefined
                || card.counts['ACTION_CHECKED'] != undefined || card.counts['ACTION_UNCHECKED'] != undefined;
        };


        ctrl.isEmpty = function(obj) {
            return Object.keys(obj).length === 0;
        };

        //----------
        ctrl.columnsLocation = 'BOARD';

        var assignToColumn = function(columns) {
            $(".lavagna-to-be-cleaned-up").remove();
            ctrl.columns = columns;
            $rootScope.$broadcast('requestSearch');
        };

        StompClient.subscribe($scope, '/event/board/'+boardName+'/location/BOARD/column', function() {
            Board.columns(boardName, ctrl.columnsLocation).then(assignToColumn);
        });

        Board.columns(boardName, 'BOARD').then(assignToColumn);

        //-------------

        User.hasPermission('MOVE_COLUMN', projectName).then(function() {
            ctrl.sortableColumnOptions = {
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
                            Board.reorderColumn(boardName, ctrl.columnsLocation, colPos).catch(function(error) {
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
            ctrl.sortableColumnOptions = false;
        });


        User.hasPermission('MOVE_CARD', projectName).then(function() {
            ctrl.sortableCardOptions = {
                    tolerance: "pointer",
                    connectWith: ".lavagna-board-cards,.sidebar-dropzone",
                    cancel: '.lvg-not-sortable-card',
                    placeholder: "lavagna-card-placeholder",
                    start : function(e, ui) {
                        $("#sidebar-drop-zone").show();
                        ui.placeholder.height(ui.helper.outerHeight());
                        ui.item.data('initialColumnId', ui.item.attr('data-lavagna-card-column-id')); //use the current attribute
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
                        ui.item.data('newColumnId', ui.item.parents('.lavagna-sortable-board-column').attr('data-lvg-column-id'));
                        ui.item.data('hasUpdate', true);
                    },
                    //http://stackoverflow.com/a/20228500
                    over: function(e, ui) {
                        ui.item.data('sortableItem').scrollParent = ui.placeholder.parent();
                        ui.item.data('sortableItem').overflowOffset = ui.placeholder.parent().offset();
                    }
            };
        }, function() {
            ctrl.sortableCardOptions = false;
        });




        //will be used as a map columnState[columnId].editColumnName = true/false
        ctrl.columnState = {};

        ctrl.createColumn = function(columnToCreate) {
            Board.createColumn(boardName, columnToCreate).then(function() {
                columnToCreate.name = null;
                columnToCreate.definition = null;
            }).catch(function(error) {
                Notification.addAutoAckNotification('error', { key : 'notification.board.create-column.error'}, false);
            });
        };

        //-----------------------------------------------------------------------------------------------------

        var formatBulkRequest = function() {
            var r = {};
            r[projectName] = selectedVisibleCardsId();
            return r;
        };

        ctrl.formatBulkRequest = formatBulkRequest;
        ctrl.selectedVisibleCardsIdByColumnId = selectedVisibleCardsIdByColumnId;

        //-----------------------------------------------------------------------------------------------------

        //some sidebar controls
        ctrl.toggledSidebar = false;

        ctrl.toggleSidebar = function() {
            ctrl.toggledSidebar = !ctrl.toggledSidebar;
        };
    }
})();
