(function() {

    'use strict';

    angular.module('lavagna.components').component('lvgStatsPanelBoard', {
        template : '<lvg-stats-panel-simple item="$ctrl.board" stats-fetcher="$ctrl.statsFetcher()">' +
                       '<a data-ui-sref="board({projectName: $ctrl.projectShortName, shortName: $ctrl.board.shortName})">{{$ctrl.board.shortName}} - {{$ctrl.board.name}}</a>' +
                   '</lvg-stats-panel-simple>',
        bindings : {
            board: '<',
            projectShortName: '<'
        },
        controller : ['Board', StatsPanelBoardController]
    });


    function StatsPanelBoardController(Board) {

        var ctrl = this;

        ctrl.statsFetcher = statsFetcher;

        function statsFetcher() {
            return Board.taskStatistics(ctrl.board.shortName);
        }
    }

})();
