(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardSidebar', {
    	bindings: {
            board: '<',
            project: '<',
            sideBarLocation: '<',
            metadataRef: '&',
            user: '<'
        },
        templateUrl: 'app/components/board/sidebar/board-sidebar.html',
        controller: BoardSidebarController,
    });


    function BoardSidebarController($scope, Board, Card, StompClient, SharedBoardDataService) {

        var ctrl = this;
        var boardShortName = ctrl.board.shortName;
        var projectShortName = ctrl.project.shortName;

        ctrl.sidebarLoaded = false;

        ctrl.$onInit = function() {
            switchLocation();
        };

        ctrl.$onChanges = function(changes) {
            if(changes.sideBarLocation && !changes.sideBarLocation.isFirstChange()) {
                switchLocation();
            }
        };

        ctrl.dropCard = dropCard;

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
            ctrl.sidebar = ctrl.sidebar || {currentPage: 0};

            Board.cardsInLocationPaginated(boardShortName, ctrl.sideBarLocation, ctrl.sidebar.currentPage + direction).then(function (res) {
                if (res.length === 0) {
                    Board.cardsInLocationPaginated(boardShortName, ctrl.sideBarLocation, 0).then(function (res) {
                        ctrl.sidebar = {currentPage: 0, cards: res.slice(0, 10), hasMore: res.length === 11};
                    });
                } else {
                    ctrl.sidebar = {currentPage: ctrl.sidebar.currentPage + direction, cards: res.slice(0, 10), hasMore: res.length === 11};
                }
                ctrl.sidebarLoaded = true;
            });
        }

        ctrl.nextPage = function() {
            sideBarLoad(1);
        };

        ctrl.prevPage = function() {
            sideBarLoad(-1);
        };

        ctrl.dragStartCard = function(item) {
        	SharedBoardDataService.dndColumnOrigin = ctrl;
        	SharedBoardDataService.dndCardOrigin = item;
        }

        ctrl.removeCard = function(card) {
        	var cards = ctrl.sidebar.cards;
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

        var subscriptionScope;

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
