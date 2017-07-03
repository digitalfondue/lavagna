(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardColumn', {
        bindings: {
            projectShortName: '@',
            metadataRef: '&',
            boardShortName: '@',
            column: '=',
            selectedCards: '=',
            searchFilterRef: '&',
            createNewCardAdvanced: '&',
            userRef: '&'
        },
        templateUrl: 'app/components/board/column/board-column.html',
        controller: ['$filter', '$mdDialog', '$translate', 'Board', 'Card', 'Notification', 'StompClient', 'BulkOperations', 'SharedBoardDataService', 'EventBus', BoardColumnController],
    });

    function BoardColumnController($filter, $mdDialog, $translate, Board, Card, Notification, StompClient, BulkOperations, SharedBoardDataService, EventBus) {
        var ctrl = this;

        //
        ctrl.dragStartCard = dragStartCard;
        ctrl.dragEndCard = dragEndCard;
        ctrl.removeCard = removeCard;
        ctrl.dropCard = dropCard ;
        ctrl.selectAllInColumn = selectAllInColumn;
        ctrl.unSelectAllInColumn = unSelectAllInColumn;
        ctrl.assignToCurrentUser = assignToCurrentUser;
        ctrl.removeAssignForCurrentUser = removeAssignForCurrentUser;
        ctrl.watchCard = watchCard;
        ctrl.unWatchCard = unWatchCard;
        ctrl.moveColumn = moveColumn;
        ctrl.saveNewColumnName = saveNewColumnName;
        ctrl.setColumnDefinition = setColumnDefinition;
        ctrl.moveAllCardsInColumn = moveAllCardsInColumn;
        //

        var stompSub = angular.noop;
        var selectAllSub = angular.noop;
        var unselectAllSub = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.user = ctrl.userRef();
            ctrl.searchFilter = ctrl.searchFilterRef();
            ctrl.metadata = ctrl.metadataRef();

            initializeColumn();
            // capture all status variables
            ctrl.columnState = {};
            selectAllSub = EventBus.on('selectall', selectAllInColumn);
            unselectAllSub = EventBus.on('unselectall', unSelectAllInColumn);
        };

        ctrl.$onDestroy = function onDestroy() {
            stompSub();
            selectAllSub();
            unselectAllSub();
        };
        //

        function initializeColumn() {
            var columnId = ctrl.column.id;

            function loadCards() {
                Card.findByColumn(columnId).then(function (res) {
                    res.columnId = columnId;
                    ctrl.cardsInColumn = res;
                    ctrl.loaded = true;

                    // sync selection, in case of a moved selected card
                    // not optimal, but it should be good enough
                    if (ctrl.selectedCards[ctrl.column.id]) {
                        for (var key in ctrl.selectedCards[ctrl.column.id]) {
                            if (ctrl.selectedCards[ctrl.column.id][key] && !idExist(parseInt(key))) {
                                delete ctrl.selectedCards[ctrl.column.id][key];
                            }
                        }
                    }

                    function idExist(id) {
                        for (var i = 0; i < ctrl.cardsInColumn.length;i++) {
                            if (ctrl.cardsInColumn[i].id === id) {
                                return true;
                            }
                        }

                        return false;
                    }

                    //
                });
            }

            stompSub = StompClient.subscribe('/event/column/' + columnId + '/card', loadCards);
            loadCards();
        }

        function dragStartCard(item) {
            SharedBoardDataService.startDrag();
            SharedBoardDataService.dndColumnOrigin = ctrl;
            SharedBoardDataService.dndCardOrigin = item;
        }

        function dragEndCard() {
            SharedBoardDataService.endDrag();
        }

        function removeCard(card) {
            for (var i = 0; i < ctrl.cardsInColumn.length; i++) {
                if (ctrl.cardsInColumn[i].id === card.id) {
                    ctrl.cardsInColumn.splice(i, 1);
                    break;
                }
            }
        }

        function dropCard(index) {
            var card = SharedBoardDataService.dndCardOrigin;

            SharedBoardDataService.dndCardOrigin = null;
            if (!card) {
                return;
            }
            // ignore drop as it's the same position
            if (card.columnId === ctrl.column.id && ctrl.cardsInColumn[index] && ctrl.cardsInColumn[index].id === card.id) {
                return;
            }

            // remove card from origin column
            if (SharedBoardDataService.dndColumnOrigin) {
                SharedBoardDataService.dndColumnOrigin.removeCard(card);
                SharedBoardDataService.dndColumnOrigin = null;
            }

            // insert card at correct index
            ctrl.cardsInColumn.splice(index, 0, card);
            //

            var oldColumnId = card.columnId;
            var newColumnId = ctrl.column.id;
            var cardId = card.id;
            var ids = [];

            angular.forEach(ctrl.cardsInColumn, function (card) {
                ids.push(card.id);
            });

            if (oldColumnId === newColumnId) {
                // internal reorder
                Board.updateCardOrder(ctrl.boardShortName, oldColumnId, ids).catch(function () {
                    Notification.addAutoAckNotification('error', { key: 'notification.generic.error'}, false);
                });
            } else {
                // move card from one column to another
                Board.moveCardToColumn(cardId, oldColumnId, newColumnId, {newContainer: ids}).catch(function () {
                    Notification.addAutoAckNotification('error', { key: 'notification.generic.error'}, false);
                });
            }
        }

        function selectAllInColumn() {
            angular.forEach($filter('filter')(ctrl.cardsInColumn, ctrl.searchFilter.cardFilter), function (c) {
                if (!ctrl.selectedCards[ctrl.column.id]) {
                    ctrl.selectedCards[ctrl.column.id] = {};
                }
                ctrl.selectedCards[ctrl.column.id][c.id] = true;
            });
            EventBus.emit('updatecheckbox');
        }

        function unSelectAllInColumn() {
            angular.forEach($filter('filter')(ctrl.cardsInColumn, ctrl.searchFilter.cardFilter), function () {
                delete ctrl.selectedCards[ctrl.column.id];
            });
            EventBus.emit('updatecheckbox');
        }

        function assignToCurrentUser(cardId, currentUserId) {
            var cardByProject = {};

            cardByProject[ctrl.projectShortName] = [cardId];
            BulkOperations.assign(cardByProject, {id: currentUserId});
        }

        function removeAssignForCurrentUser(cardId, currentUserId) {
            var cardByProject = {};

            cardByProject[ctrl.projectShortName] = [cardId];
            BulkOperations.removeAssign(cardByProject, {id: currentUserId});
        }

        function watchCard(cardId, currentUserId) {
            var cardByProject = {};

            cardByProject[ctrl.projectShortName] = [cardId];
            BulkOperations.watch(cardByProject, {id: currentUserId});
        }

        function unWatchCard(cardId, currentUserId) {
            var cardByProject = {};

            cardByProject[ctrl.projectShortName] = [cardId];
            BulkOperations.unWatch(cardByProject, {id: currentUserId});
        }

        function moveColumn(location) {
            var confirmAction = function () {
                Board.moveColumnToLocation(ctrl.column.id, location).catch(function () {
                    Notification.addAutoAckNotification('error', { key: 'notification.generic.error'}, false);
                });
            };

            var title = $translate.instant('board.column.operation.move-column-to-location', {columnName: ctrl.column.name, location: $filter('capitalize')(location)});
            var confirm = $mdDialog.confirm()
                .title(title)
                .ariaLabel(title)
                .ok($translate.instant('button.yes'))
                .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                confirmAction();
            }, function () {});
        }

        function moveAllCardsInColumn(cards, location) {
            var cardIds = cards.map(function (c) { return c.id; });
            var confirmAction = function () {
                Card.moveAllFromColumnToLocation(ctrl.column.id, cardIds, location).catch(function () {
                    Notification.addAutoAckNotification('error', { key: 'notification.generic.error'}, false);
                });
            };

            var title = $translate.instant('board.column.operation.move-card-from-column-to-location', {columnName: ctrl.column.name, location: $filter('capitalize')(location)});
            var confirm = $mdDialog.confirm()
                .title(title)
                .ariaLabel(title)
                .ok($translate.instant('button.yes'))
                .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                confirmAction();
            }, function () {});
        }

        function saveNewColumnName(newName) {
            Board.renameColumn(ctrl.boardShortName, ctrl.column.id, newName).catch(function () {
                Notification.addAutoAckNotification('error', { key: 'notification.board.rename-column.error'}, false);
            });
        }

        function setColumnDefinition(definition) {
            Board.redefineColumn(ctrl.boardShortName, ctrl.column.id, definition).catch(function () {
                Notification.addAutoAckNotification('error', { key: 'notification.board.redefine-column.error'}, false);
            });
        }
    }
}());
