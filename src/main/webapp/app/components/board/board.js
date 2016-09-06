(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoard', {
        controller: BoardController,
        bindings: {
            project: '=',
            board: '=',
            userReference: '&'
        },
        templateUrl: 'app/components/board/board.html'
    });

    function BoardController($rootScope, $scope, $location, $filter, $log, $timeout,
        Board, Card, Project, LabelCache, Search, StompClient, User, Notification) {

        var ctrl = this;
        ctrl.searchFilter = {};

        var boardName = ctrl.board.shortName;
        var projectName = ctrl.project.shortName;

        ctrl.user = ctrl.userReference();
        ctrl.currentUserId = ctrl.user.id;

        Project.loadMetadataAndSubscribe(projectName, ctrl, $scope);
        
        ctrl.moveCard = function(card, location) {
            Card.moveAllFromColumnToLocation(card.columnId, [card.id], location);
        };

        ctrl.backFromLocation = function() {
            ctrl.locationOpened=false;
            ctrl.sideBarLocation=undefined;
        };

        ctrl.dropColumn = function(index, oldIndex) {
        	var currentColIdx = oldIndex;
        	var column = ctrl.columns[oldIndex];
        	//same position, ignore drop
        	if(currentColIdx == index) {
        		return false;
        	}
        	
        	ctrl.columns.splice(currentColIdx, 1);
        	
        	ctrl.columns.splice(index, 0, column);
        	
        	var colPos = [];
        	angular.forEach(ctrl.columns, function(col) {
        		colPos.push(col.id);
        	});
        	
        	Board.reorderColumn(boardName, ctrl.columnsLocation, colPos).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
        	});
            
        }
        //

        //keep track of the selected cards
        ctrl.selectedCards = {};
        //ctrl.foundCards = {};


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
            });
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
                        ctrl.query = searchFilter.location.q;
                        $timeout(function() {
                            $location.search(searchFilter.location);
                            $scope.$broadcast('updatedQueryOrPage', searchFilter);
                        });
                    });

                } catch(e) {
                    $log.debug('parsing exception', e);
                }
            });
        });
        //

        //-----------------------------------------------------------------------------------------------------

        //----------
        ctrl.columnsLocation = 'BOARD';

        var assignToColumn = function(columns) {
            ctrl.columns = columns;
            $rootScope.$broadcast('requestSearch');
        };

        StompClient.subscribe('/event/board/'+boardName+'/location/BOARD/column', function() {
            Board.columnsByLocation(boardName, ctrl.columnsLocation).then(assignToColumn);
        }, $scope);

        Board.columnsByLocation(boardName, 'BOARD').then(assignToColumn);

        //-------------        

        //will be used as a map columnState[columnId].editColumnName = true/false
        ctrl.columnState = {};



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
