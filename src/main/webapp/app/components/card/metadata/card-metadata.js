(function() {
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
        controller: CardMetadataController,
        templateUrl: 'app/components/card/metadata/card-metadata.html'
    });

    var COMPONENT_PERMISSIONS = ['UPDATE_CARD'];

    function CardMetadataController($rootScope, $scope, CardCache, Card, User, LabelCache, Label, StompClient,
        Notification, Board, BulkOperations, Project, Search) {
        var ctrl = this;

        // return the current card in a bulk operation friendly data structure
        var currentCard = function() {
            var cardByProject = {};
            cardByProject[ctrl.project.shortName] = [ctrl.card.id];
            return cardByProject;
        };
        //

        var findAndAssignColumns = function() {
            Board.columns(ctrl.board.shortName).then(function(cols) {
                var locations = [];
                var columns = [];
                var column = null;
                for(var i = 0; i < cols.length; i++) {
                    var col = cols[i];
                    if(col.location === 'BOARD') {
                        columns.push(col);
                    } else {
                        if(col.name === 'ARCHIVE' || col.name === 'BACKLOG' || col.name === 'TRASH') {
                            locations.push(col);
                        }
                    }

                    if(col.id === ctrl.card.columnId) {
                        column = col;
                    }
                }

                ctrl.locations = locations;
                ctrl.columns = columns;
                ctrl.column = column;
            });
        };

        StompClient.subscribe($scope, '/event/board/'+ctrl.board.shortName+'/location/BOARD/column', findAndAssignColumns);

        findAndAssignColumns();
        //

        ctrl.moveCard = function(column) {
            if(angular.isUndefined(column)) {
                return;
            }
            if(column.id === ctrl.card.columnId) {
                return;
            }

            if(column.location === 'BOARD') {
                Board.moveCardToColumnEnd(ctrl.card.id, ctrl.card.columnId, column.id).then(function() {
                    Notification.addAutoAckNotification('success', {
                        key: 'notification.card.moveToColumn.success',
                        parameters: { columnName: column.name }
                    }, false);
                    $rootScope.$emit('card.moved.event');
                }, function(error) {
                    findAndAssignColumns();
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.card.moveToColumn.error',
                        parameters: { columnName: column.name }
                    }, false);
                });

            } else {
                Card.moveAllFromColumnToLocation(ctrl.card.columnId, [ctrl.card.id], column.location).then(function() {
                    Notification.addAutoAckNotification('success', {
                        key: 'notification.card.moveToLocation.success',
                        parameters: { location: column.location }
                    }, false);
                    $rootScope.$emit('card.moved.event');
                }, function(error) {
                    findAndAssignColumns();
                    Notification.addAutoAckNotification('error', {
                        key: 'notification.card.moveToLocation.error',
                        parameters: { location: column.location }
                    }, false);
                })
            }
        };
        //

        ctrl.setDueDate = function(date) {
            BulkOperations.setDueDate(currentCard(), date)
        };

        ctrl.removeDueDate = function() {
            BulkOperations.removeDueDate(currentCard())
        };

        // ----
        ctrl.hasClosedMilestones = function() {
            for(var i = 0; i < ctrl.project.metadata.milestones.length; i++) {
                if(ctrl.project.metadata.milestones[i].status === 'CLOSED') {
                    return true;
                }
            }
            return false;
        };

        ctrl.setMilestone = function(milestone) {
            BulkOperations.setMilestone(currentCard(), milestone);
        };

        ctrl.removeMilestone = function() {
            BulkOperations.removeMilestone(currentCard());
        };
        // ----

        ctrl.userPermissions = {};
        function loadUserPermissions() {
            User.hasPermissions(COMPONENT_PERMISSIONS, ctrl.project.shortName).then(function(permissions) {
                ctrl.userPermissions = permissions;
            });
        }
        loadUserPermissions();
    };
})();
