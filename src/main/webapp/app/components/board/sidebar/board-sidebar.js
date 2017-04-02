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
        controller: ['Board', 'Card', 'StompClient', 'SharedBoardDataService', BoardSidebarController],
    });

    function BoardSidebarController(Board, Card, StompClient, SharedBoardDataService) {
        var ctrl = this;

        //
        ctrl.dropCard = dropCard;
        ctrl.nextPage = nextPage;
        ctrl.prevPage = prevPage;
        ctrl.dragStartCard = dragStartCard;
        ctrl.removeCard = removeCard;
        //

        var startDragListener = angular.noop;
        var stopDragListener = angular.noop;
        var stompSubscription = angular.noop;

        ctrl.$onInit = function () {
            ctrl.sidebarLoaded = false;

            switchLocation();

            startDragListener = SharedBoardDataService.listenToDragStart(function () {
                ctrl.dragFromBoard = true;
            });
            stopDragListener = SharedBoardDataService.listenToDragEnd(function () {
                ctrl.dragFromBoard = false;
            });
        };

        ctrl.$onChanges = function (changes) {
            if (changes.sideBarLocation && !changes.sideBarLocation.isFirstChange()) {
                switchLocation();
            }
        };

        ctrl.$onDestroy = function () {
            startDragListener();
            stopDragListener();
            stompSubscription();
        };

        function sideBarLoad(direction) {
            ctrl.sidebar = ctrl.sidebar || {currentPage: 0};

            Board.cardsInLocationPaginated(ctrl.board.shortName, ctrl.sideBarLocation, ctrl.sidebar.currentPage + direction).then(function (res) {
                if (res.length === 0) {
                    Board.cardsInLocationPaginated(ctrl.board.shortName, ctrl.sideBarLocation, 0).then(function (res) {
                        ctrl.sidebar = {currentPage: 0, cards: res.slice(0, 10), hasMore: res.length === 11};
                    });
                } else {
                    ctrl.sidebar = {currentPage: ctrl.sidebar.currentPage + direction, cards: res.slice(0, 10), hasMore: res.length === 11};
                }
                ctrl.sidebarLoaded = true;
            });
        }

        function nextPage() {
            sideBarLoad(1);
        }

        function prevPage() {
            sideBarLoad(-1);
        }

        function dragStartCard(item) {
            SharedBoardDataService.dndColumnOrigin = ctrl;
            SharedBoardDataService.dndCardOrigin = item;
        }

        function removeCard(card) {
            var cards = ctrl.sidebar.cards;

            for (var i = 0; i < cards.length; i++) {
                if (cards[i].id === card.id) {
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
            if (stompSubscription !== angular.noop) {
                stompSubscription();
                stompSubscription = angular.noop;
            }

            if (ctrl.sideBarLocation && ctrl.sideBarLocation !== 'NONE') {
                sideBarLoad(0);

                stompSubscription = StompClient.subscribe('/event/board/' + ctrl.board.shortName + '/location/' + ctrl.sideBarLocation + '/card', function () {
                    sideBarLoad(0);
                });
            }
        }
    }
}());
