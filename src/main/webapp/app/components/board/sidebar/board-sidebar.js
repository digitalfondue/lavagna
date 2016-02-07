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
                $("#sidebar-drop-zone").sortable({
                    receive: function (ev, ui) {
                        var cardId = ui.item.attr('data-lvg-card-id');
                        ui.item.data('hasUpdate', false); // disable the move card to column logic
                        ui.item.hide();
                        if (cardId !== undefined) {
                            Card.moveAllFromColumnToLocation(ui.item.attr('data-lavagna-card-column-id'), [cardId], boardSidebarCtrl.sideBarLocation);
                        }
                    }
                });

                $("#sidebar-drop-zone").hide();
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
                    });
                } else {
                    ctrl.sidebar[ctrl.sideBarLocation] = {currentPage: ctrl.sidebar[ ctrl.sideBarLocation].currentPage + direction, found: res.slice(0, 10), hasMore: res.length === 11};
                }
                ctrl.sidebarLoaded = true;
            });
        };

        User.hasPermission('MOVE_CARD', projectShortName).then(function () {
           ctrl.sortableCardOptionsForSidebar = {
                connectWith: ".lavagna-board-cards",
                placeholder: "lavagna-card-placeholder",
                start: function (e, ui) {
                    ui.placeholder.height(ui.helper.outerHeight());
                    ui.item.data('initialColumnId', ui.item.attr('data-lavagna-card-column-id'));

                },
                stop: function (e, ui) {
                    if (ui.item.data('hasUpdate')) {
                        var cardId = parseInt(ui.item.attr('data-lvg-card-id'), 10);
                        var oldColumnId = parseInt(ui.item.data('initialColumnId'), 10);
                        var newColumnId = parseInt(ui.item.data('newColumnId'), 10);
                        var ids = ui.item.parent().sortable("toArray", {attribute: 'data-lvg-card-id'}).map(function (i) {
                            return parseInt(i, 10);
                        });
                        ui.item.addClass('lavagna-to-be-cleaned-up');
                        ui.item.replaceWith(ui.item.clone());
                        Board.moveCardToColumn(cardId, oldColumnId, newColumnId, {newContainer: ids});
                    }
                    ui.item.removeData('hasUpdate');
                    ui.item.removeData('initialColumnId');
                    ui.item.removeData('newColumnId');
                },
                update: function (e, ui) {
                    ui.item.data('newColumnId', ui.item.parent().parent().parent().attr('data-lvg-column-id'));
                    ui.item.data('hasUpdate', true);
                }
            };
        }, function () {
            ctrl.sortableCardOptionsForSidebar = false;
        });
    }

})();
