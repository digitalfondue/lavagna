(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgBoardSidebar', BoardSidebarComponent);

    function BoardSidebarComponent(Board, Card, User, StompClient) {
        return {
            restrict: 'E',
            controller: BoardSidebarController,
            controllerAs: 'boardSidebarCtrl',
            scope: true,
            bindToController: {
                board: '=',
                project: '='
            },
            templateUrl: 'app/components/board/sidebar/board-sidebar.html',
            link: function(scope, element, attrs, boardSidebarCtrl) {
                boardSidebarCtrl.sideBarLocation = 'ARCHIVE';
                boardSidebarCtrl.switchLocation();
            }

        }
    }

    function BoardSidebarController($scope, Board, Card, User, StompClient) {

        var ctrl = this;
        var boardShortName = ctrl.board.shortName;
        var projectShortName = ctrl.project.shortName;

        var subscriptionScope;

        ctrl.switchLocation = function () {
            if (subscriptionScope !== undefined) {
                subscriptionScope.$destroy();
            }

            if(ctrl.sideBarLocation && ctrl.sideBarLocation != 'NONE') {
                subscriptionScope = $scope.$new();
                ctrl.sideBarLoad(0);

                StompClient.subscribe(subscriptionScope, '/event/board/' + boardShortName + '/location/' + ctrl.sideBarLocation + '/card', function () {
                    ctrl.sideBarLoad(0);
                });
            }
        };


        ctrl.sideBarLoad = function (direction) {
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
        
        ctrl.cardMove = function($item, $partFrom, $partTo, $indexFrom, $indexTo) {
        	console.log(arguments);
        	//move card from sidebar to column
        	if($partTo.hasOwnProperty('columnId')) {
        		
        		var cardId = $item.id;
        		var oldColumnId = $item.columnId;
        		var newColumnId = $partTo.columnId;
        		var ids = [];
        		angular.forEach($partTo, function(card) {
        			ids.push(card.id);
        		});
        		
        		Board.moveCardToColumn(cardId, oldColumnId, newColumnId, {newContainer: ids});
        	}
        };
    }

})();
