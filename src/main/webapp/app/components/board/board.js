(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoard', {
        bindings: {
            project: '<',
            board: '<',
            userReference: '&'
        },
        templateUrl: 'app/components/board/board.html',
        controller: ['$rootScope', '$scope', '$location', '$filter', '$log', '$timeout',
                     'Board', 'Card', 'Project', 'LabelCache', 'Search', 'StompClient', 'User', 'Notification', BoardController],
    });

    function BoardController($rootScope, $scope, $location, $filter, $log, $timeout,
        Board, Card, Project, LabelCache, Search, StompClient, User, Notification) {

        var ctrl = this;
        //
        ctrl.moveCard = moveCard;
        ctrl.backFromLocation = backFromLocation;
        ctrl.dropColumn = dropColumn;
        ctrl.selectAll = selectAll;
        ctrl.unSelectAll = unSelectAll;
        ctrl.toggleSidebar = toggleSidebar;
        ctrl.formatBulkRequest = formatBulkRequest;
        ctrl.selectedVisibleCardsIdByColumnId = selectedVisibleCardsIdByColumnId;
        ctrl.selectedVisibleCount = selectedVisibleCount;
        //
        
        
        var metadataSubscription = angular.noop;
        var stompSub = angular.noop;
        
        ctrl.$onInit = function init() {
        	ctrl.searchFilter = {};
            ctrl.user = ctrl.userReference();
            ctrl.currentUserId = ctrl.user.id;
            
            metadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl);
            
            // keep track of the selected cards
            ctrl.selectedCards = {};
            ctrl.columnsLocation = 'BOARD';
            
            stompSub = StompClient.subscribe('/event/board/'+ctrl.board.shortName+'/location/BOARD/column', function() {
                Board.columnsByLocation(ctrl.board.shortName, ctrl.columnsLocation).then(assignToColumn);
            });

            Board.columnsByLocation(ctrl.board.shortName, 'BOARD').then(assignToColumn);

            //-------------        

            //will be used as a map columnState[columnId].editColumnName = true/false
            ctrl.columnState = {};
            
            ctrl.toggledSidebar = false;
        }
        
        ctrl.$onDestroy = function onDestroy() {
        	metadataSubscription();
        	stompSub();
        }
        
        
        function moveCard(card, location) {
            Card.moveAllFromColumnToLocation(card.columnId, [card.id], location);
        }

        function backFromLocation() {
            ctrl.locationOpened=false;
            ctrl.sideBarLocation=undefined;
        }

        function dropColumn(index, oldIndex) {
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
        	
        	Board.reorderColumn(ctrl.board.shortName, ctrl.columnsLocation, colPos).catch(function(error) {
        		Notification.addAutoAckNotification('error', { key : 'notification.generic.error'}, false);
        	});
            
        }
        //

        function selectAll() {
            $scope.$broadcast('selectall');
        }

        function unSelectAll() {
            $scope.$broadcast('unselectall');
        }

        function selectedVisibleCardsId() {
            var ids = [];

            angular.forEach(selectedVisibleCardsIdByColumnId(), function(val) {
            	ids = ids.concat(val);
            });

            return ids;
        }

        function selectedVisibleCardsIdByColumnId() {
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
        }

        function selectedVisibleCount() {
        	return selectedVisibleCardsId().length;
        }


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

        function assignToColumn(columns) {
            ctrl.columns = columns;
            $rootScope.$broadcast('requestSearch');
        };

        //-----------------------------------------------------------------------------------------------------

        function formatBulkRequest() {
            var r = {};
            r[ctrl.project.shortName] = selectedVisibleCardsId();
            return r;
        };

        //-----------------------------------------------------------------------------------------------------

        //some sidebar controls

        function toggleSidebar() {
            ctrl.toggledSidebar = !ctrl.toggledSidebar;
        };
    }
})();
