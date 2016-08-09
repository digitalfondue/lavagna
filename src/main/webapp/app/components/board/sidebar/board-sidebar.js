(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardSidebar', {
    	bindings: {
            board: '=',
            project: '=',
            sideBarLocation: '=',
            user: '=',
            metadataRef:'&'
        },
        templateUrl: 'app/components/board/sidebar/board-sidebar.html',
        controller: BoardSidebarController,
    });


    function BoardSidebarController($scope, Board, Card, StompClient, SharedBoardDataService) {

        var ctrl = this;
        
        var boardShortName = ctrl.board.shortName;
        var projectShortName = ctrl.project.shortName;

        var subscriptionScope;

        ctrl.switchLocation = switchLocation;
        ctrl.sideBarLoad = sideBarLoad;
        ctrl.dropCard = dropCard;
        
        switchLocation();
        
        $scope.$watch('$ctrl.sideBarLocation', function() {
        	switchLocation();
        });
        
        var startDragListener = SharedBoardDataService.listenToDragStart(function() {
        	ctrl.dragFromBoard = true;
        });
        
        var stopDragListener = SharedBoardDataService.listenToDragEnd(function() {
        	ctrl.dragFromBoard = false;
        });
        
        
        ctrl.$onDestroy = function() {
        	startDragListener();
        	stopDragListener();
        }


        function sideBarLoad(direction) {
            ctrl.sidebarLoaded = false;
            ctrl.sidebar = ctrl.sidebar || {};
            if (ctrl.sidebar[ctrl.sideBarLocation] === undefined) {
                ctrl.sidebar[ctrl.sideBarLocation] = {currentPage: 0};
            }
            Board.cardsInLocationPaginated(boardShortName, ctrl.sideBarLocation, ctrl.sidebar[ctrl.sideBarLocation].currentPage + direction).then(function (res) {
                if (res.length === 0) {
                    Board.cardsInLocationPaginated(boardShortName, ctrl.sideBarLocation, 0).then(function (res) {
                        ctrl.sidebar[ctrl.sideBarLocation] = {currentPage: 0, found: res.slice(0, 10), hasMore: res.length === 11};
                        ctrl.sidebar[ctrl.sideBarLocation].found.sideBarLocation = ctrl.sideBarLocation;
                    });
                } else {
                    ctrl.sidebar[ctrl.sideBarLocation] = {currentPage: ctrl.sidebar[ ctrl.sideBarLocation].currentPage + direction, found: res.slice(0, 10), hasMore: res.length === 11};
                    ctrl.sidebar[ctrl.sideBarLocation].found.sideBarLocation = ctrl.sideBarLocation;
                }
                ctrl.sidebarLoaded = true;
            });
        };
        
        ctrl.dragStartCard = function(item) {
        	SharedBoardDataService.dndColumnOrigin = ctrl;
        	SharedBoardDataService.dndCardOrigin = item;
        }
        
        ctrl.removeCard = function(card) {
        	var cards = ctrl.sidebar[ctrl.sideBarLocation].found;
        	for(var i = 0; i < cards.length; i++) {
        		if(cards[i].id === card.id) {
        			cards.splice(i, 1);
        			break;
        		}
        	}
        }
        
        function dropCard() {
        	var card = SharedBoardDataService.dndCardOrigin;
        	Card.moveAllFromColumnToLocation(card.columnId, [card.id], ctrl.sideBarLocation);
        }
        
        function switchLocation() {
            if (subscriptionScope !== undefined) {
                subscriptionScope.$destroy();
            }

            if(ctrl.sideBarLocation && ctrl.sideBarLocation != 'NONE') {
                subscriptionScope = $scope.$new();
                sideBarLoad(0);

                StompClient.subscribe(subscriptionScope, '/event/board/' + boardShortName + '/location/' + ctrl.sideBarLocation + '/card', function () {
                    sideBarLoad(0);
                });
            }
        };
    }

})();
