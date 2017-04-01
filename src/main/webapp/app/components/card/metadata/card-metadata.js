(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardMetadata', {
        bindings: {
            card: '<',
            board: '<',
            project: '<',
            user: '<',
            milestones: '<',
            dueDates: '<'
        },
        templateUrl: 'app/components/card/metadata/card-metadata.html',
        controller: ['Card', 'User', 'StompClient', 'Notification', 'Board', 'BulkOperations', CardMetadataController]
    });

    var COMPONENT_PERMISSIONS = ['UPDATE_CARD', 'MOVE_CARD'];

    function CardMetadataController(Card, User, StompClient, Notification, Board, BulkOperations) {
        var ctrl = this;
        //

        ctrl.moveCard = moveCard;
        ctrl.setDueDate = setDueDate;
        ctrl.removeDueDate = removeDueDate;
        ctrl.hasClosedMilestones = hasClosedMilestones;
        ctrl.setMilestone = setMilestone;
        ctrl.removeMilestone = removeMilestone;
        //

        var stompSubscription = angular.noop;

        ctrl.$onInit = function init() {
            stompSubscription = StompClient.subscribe('/event/board/' + ctrl.board.shortName + '/location/BOARD/column', findAndAssignColumns);
            findAndAssignColumns();
            ctrl.userPermissions = {};
            loadUserPermissions();
        };

        ctrl.$onDestroy = function onDestroy() {
            stompSubscription();
        };

        // return the current card in a bulk operation friendly data structure
        function currentCard() {
            var cardByProject = {};

            cardByProject[ctrl.project.shortName] = [ctrl.card.id];

            return cardByProject;
        }
        //

        function findAndAssignColumns() {
            Board.columns(ctrl.board.shortName).then(function (cols) {
                var locations = [];
                var columns = [];
                var column = null;

                for (var i = 0; i < cols.length; i++) {
                    var col = cols[i];

                    if (col.location === 'BOARD') {
                        columns.push(col);
                    } else if (col.name === 'ARCHIVE' || col.name === 'BACKLOG' || col.name === 'TRASH') {
                        locations.push(col);
                    }

                    if (col.id === ctrl.card.columnId) {
                        column = col;
                    }
                }

                ctrl.locations = locations;
                ctrl.columns = columns;
                ctrl.column = column;
            });
        }

        //
        function moveCard(column) {
            if (angular.isUndefined(column)) {
                return;
            }
            if (column.id === ctrl.card.columnId) {
                return;
            }

            if (column.location === 'BOARD') {
                Board.moveCardToColumnEnd(ctrl.card.id, ctrl.card.columnId, column.id).then(function () {
                    Notification.addAutoAckNotification('success', {
                        key: 'notification.card.moveToColumn.success',
                        parameters: { columnName: column.name }
                    }, false);
                }, function () {
                    findAndAssignColumns();
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.card.moveToColumn.error',
                        parameters: { columnName: column.name }
                    }, false);
                });
            } else {
                Card.moveAllFromColumnToLocation(ctrl.card.columnId, [ctrl.card.id], column.location).then(function () {
                    Notification.addAutoAckNotification('success', {
                        key: 'notification.card.moveToLocation.success',
                        parameters: { location: column.location }
                    }, false);
                }, function () {
                    findAndAssignColumns();
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.card.moveToLocation.error',
                        parameters: { location: column.location }
                    }, false);
                });
            }
        }
        //

        function setDueDate(date) {
            BulkOperations.setDueDate(currentCard(), date);
        }

        function removeDueDate() {
            BulkOperations.removeDueDate(currentCard());
        }

        // ----
        function hasClosedMilestones() {
            for (var i = 0; i < ctrl.project.metadata.milestones.length; i++) {
                if (ctrl.project.metadata.milestones[i].status === 'CLOSED') {
                    return true;
                }
            }

            return false;
        }

        function setMilestone(milestone) {
            BulkOperations.setMilestone(currentCard(), milestone);
        }

        function removeMilestone() {
            BulkOperations.removeMilestone(currentCard());
        }
        // ----

        function loadUserPermissions() {
            User.hasPermissions(COMPONENT_PERMISSIONS, ctrl.project.shortName).then(function (permissions) {
                ctrl.userPermissions = permissions;
            });
        }
    }
}());
