(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardSidebar', {
    	bindings: {
            board: '=',
            project: '=',
            sideBarLocation: '='
        },
        templateUrl: 'app/components/board/sidebar/board-sidebar.html',
        controller: BoardSidebarController
    });
    

    function BoardSidebarController($scope, Board, Card, User, StompClient) {

        var ctrl = this;
        
        var boardShortName = ctrl.board.shortName;
        var projectShortName = ctrl.project.shortName;

        var subscriptionScope;
        
        

        ctrl.switchLocation = switchLocation;
        ctrl.sideBarLoad = sideBarLoad;
        ctrl.cardMove = cardMove;
        
        switchLocation();
        
        $scope.$watch('$ctrl.sideBarLocation', function() {
        	switchLocation();
        });


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
        
        function cardMove($item, $partFrom, $partTo, $indexFrom, $indexTo) {
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
