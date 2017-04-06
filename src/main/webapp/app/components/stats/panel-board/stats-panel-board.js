(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgStatsPanelBoard', {
        bindings: {
            board: '<',
            projectShortName: '<',
            onUpdate: '&'
        },
        controller: ['Board', StatsPanelBoardController],
        templateUrl: 'app/components/stats/panel-board/stats-panel-board.html'
    });

    function StatsPanelBoardController(Board) {
        var ctrl = this;

        ctrl.statsFetcher = statsFetcher;
        ctrl.archive = archive;
        ctrl.unarchive = unarchive;

        function statsFetcher() {
            return Board.taskStatistics(ctrl.board.shortName);
        }

        function archive() {
            update(ctrl.board.shortName, ctrl.board.name, ctrl.board.description, true);
        }

        function unarchive() {
            update(ctrl.board.shortName, ctrl.board.name, ctrl.board.description, false);
        }

        function update(shortName, name, description, isArchived) {
            Board.update({
                shortName: shortName,
                name: name,
                description: description,
                archived: isArchived
            }).then(ctrl.onUpdate);
        }
    }
}());
