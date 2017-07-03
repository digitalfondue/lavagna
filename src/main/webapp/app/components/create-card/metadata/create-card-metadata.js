(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCreateCardMetadata', {
        bindings: {
            column: '<',
            boardShortName: '<',
            projectMetadata: '<',
            milestone: '<',
            dueDate: '<',
            onUpdate: '&'
        },
        templateUrl: 'app/components/create-card/metadata/create-card-metadata.html',
        controller: ['$filter', 'Board', 'Label', 'StompClient', CreateCardMetadataController]
    });

    function CreateCardMetadataController($filter, Board, Label, StompClient) {
        var ctrl = this;

        ctrl.locations = [];
        ctrl.columns = [];
        ctrl.hasClosedMilestones = hasClosedMilestones;
        ctrl.setDueDate = setDueDate;
        ctrl.setMilestone = setMileStone;

        var stompSubscription = angular.noop;

        ctrl.$onInit = function init() {
            stompSubscription = StompClient.subscribe('/event/board/' + ctrl.boardShortName + '/location/BOARD/column', findAndAssignColumns);
            findAndAssignColumns();
        };

        ctrl.$onDestroy = function onDestroy() {
            stompSubscription();
        };

        function findAndAssignColumns() {
            Board.columns(ctrl.boardShortName).then(function (cols) {
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

                    if (angular.isDefined(ctrl.column) && ctrl.column !== null && col.id === ctrl.column.id) {
                        column = col;
                    }
                }

                if (column === null) {
                    column = columns[0];
                }

                ctrl.locations = locations;
                ctrl.columns = columns;
                ctrl.column = column;

                ctrl.onUpdate({$column: ctrl.column, $dueDate: ctrl.dueDate, $milestone: ctrl.milestone});
            });
        }

        function setDueDate(dueDate) {
            var labelValue = Label.dateVal($filter('extractISO8601Date')(dueDate));

            ctrl.onUpdate({$column: ctrl.column, $dueDate: labelValue, $milestone: ctrl.milestone});
        }

        function setMileStone(milestone) {
            var labelValue = Label.listVal(milestone.id);

            ctrl.onUpdate({$column: ctrl.column, $dueDate: ctrl.dueDate, $milestone: labelValue});
        }

        function hasClosedMilestones() {
            if (!angular.isDefined(ctrl.projectMetadata)) {
                return false;
            }

            for (var i = 0; i < ctrl.projectMetadata.milestones.length; i++) {
                if (ctrl.projectMetadata.milestones[i].status === 'CLOSED') {
                    return true;
                }
            }

            return false;
        }
    }
}());
