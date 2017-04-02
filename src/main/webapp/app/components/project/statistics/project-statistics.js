(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectStatistics', {
        controller: ProjectStatisticsController,
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/statistics/project-statistics.html'
    });

    function ProjectStatisticsController($translate, $filter, Project, Board) {
        var ctrl = this;

        ctrl.showCreatedAndClosedCards = false;
        ctrl.showCardsHistory = false;
        ctrl.showCardsByLabel = false;

        ctrl.cardsHistoryChartOptions = {
            pointDot: false,
            bezierCurve: false,
            scaleIntegersOnly: true,
            showTooltips: false,
            scaleBeginAtZero: true,
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            scaleOverride: true,
            scaleSteps: 4,
            scaleStartValue: 0
        };

        ctrl.cardsByLabelChartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            animation: false
        };

        var generateDashboard = function (stats) {
            var cardsHistory = {
                labels: [],
                datasets: []
            };

            function getSortedIndexes(array) {
                var sortedIndexes = [];

                for (var index in array) {
                    if (array.hasOwnProperty(index)) {
                        sortedIndexes.push(parseInt(index));
                    }
                }
                sortedIndexes.sort();

                return sortedIndexes;
            }

            function getStatusFromHistory(stats, index, definition) {
                return stats.statusHistory[index][definition] || 0;
            }

            function createCardsHistorySeries(label, color) {
                cardsHistory.datasets.push({label: label, strokeColor: color, fillColor: color, pointColor: color, data: []});
            }

            createCardsHistorySeries('Backlog', $filter('color')(stats.backlogTaskColor).color);
            createCardsHistorySeries('Open', $filter('color')(stats.openTaskColor).color);
            createCardsHistorySeries('Deferred', $filter('color')(stats.deferredTaskColor).color);
            createCardsHistorySeries('Closed', $filter('color')(stats.closedTaskColor).color);

            var sortedHistoryIndexes = getSortedIndexes(stats.statusHistory);

            // cards history
            for (var i = 0; i < sortedHistoryIndexes.length; i++) {
                var sortedHistoryIndex = sortedHistoryIndexes[i];

                cardsHistory.labels.push(new Date(sortedHistoryIndex).toLocaleDateString());

                var closed = getStatusFromHistory(stats, sortedHistoryIndex, 'CLOSED');

                cardsHistory.datasets[3].data.push(closed);

                var deferred = closed + getStatusFromHistory(stats, sortedHistoryIndex, 'DEFERRED');

                cardsHistory.datasets[2].data.push(deferred);

                var open = deferred + getStatusFromHistory(stats, sortedHistoryIndex, 'OPEN');

                cardsHistory.datasets[1].data.push(open);

                var backlog = open + getStatusFromHistory(stats, sortedHistoryIndex, 'BACKLOG');

                cardsHistory.datasets[0].data.push(backlog);
            }

            // scale for the chart
            var maxValue = Math.max.apply(Math, cardsHistory.datasets[0].data);

            ctrl.cardsHistoryChartOptions.scaleStepWidth = maxValue > 4 ? Math.ceil(Math.max(maxValue) / 4) : 1;

            ctrl.stats = stats;

            ctrl.totalCards = stats.openTaskCount + stats.closedTaskCount + stats.deferredTaskCount + stats.backlogTaskCount;

            ctrl.chartCardsHistoryData = cardsHistory;
            ctrl.showCardsHistory = cardsHistory.labels.length > 1;

            // cards by label
            ctrl.cardsByLabelMax = 1;
            for (var j = 0; j < stats.cardsByLabel.length; j++) {
                var label = stats.cardsByLabel[j];

                ctrl.cardsByLabelMax = Math.max(ctrl.cardsByLabelMax, label.count);
            }
            ctrl.showCardsByLabel = stats.cardsByLabel.length > 0;

            // created vs closed
            ctrl.openedThisPeriod = 0;
            ctrl.closedThisPeriod = 0;

            for (var index in stats.createdAndClosedCards) {
                if (stats.createdAndClosedCards.hasOwnProperty(index)) {
                    var pair = stats.createdAndClosedCards[index];

                    ctrl.openedThisPeriod += pair['first'];
                    ctrl.closedThisPeriod += pair['second'];
                }
            }
            ctrl.showCreatedAndClosedCards = Object.keys(stats.createdAndClosedCards).length > 1;
        };

        ctrl.availableDateRanges = [];

        $translate('project.statistics.last30Days').then(function (translatedValue) {
            ctrl.availableDateRanges.push({name: translatedValue, value: moment().day(-30).toDate()});
        });
        $translate('project.statistics.last14Days').then(function (translatedValue) {
            var range = {name: translatedValue, value: moment().day(-14).toDate()};

            ctrl.availableDateRanges.push(range);
            ctrl.dateRange = range;
        });
        $translate('project.statistics.last7Days').then(function (translatedValue) {
            ctrl.availableDateRanges.push({name: translatedValue, value: moment().day(-7).toDate()});
        });
        $translate('project.statistics.thisMonth').then(function (translatedValue) {
            ctrl.availableDateRanges.push({name: translatedValue, value: moment().startOf('month').toDate()});
        });
        $translate('project.statistics.thisWeek').then(function (translatedValue) {
            ctrl.availableDateRanges.push({name: translatedValue, value: moment().startOf('week').toDate()});
        });

        ctrl.filterByBoard = function (board) {
            if (angular.equals(ctrl.boards[0], board)) {
                Project.statistics(ctrl.project.shortName, ctrl.dateRange.value).then(generateDashboard);
            } else {
                Board.statistics(board.shortName, ctrl.dateRange.value).then(generateDashboard);
            }
        };

        ctrl.changeDateRange = function (range) {
            ctrl.dateRange = range;
            ctrl.filterByBoard(ctrl.boardToFilter);
        };

        Project.findBoardsInProject(ctrl.project.shortName).then(function (boards) {
            boards.unshift({name: 'All', archived: false});
            ctrl.boards = boards;
            ctrl.boardToFilter = boards[0];
            ctrl.filterByBoard(ctrl.boardToFilter);
        });

        Project.getMetadata(ctrl.project.shortName).then(function (metadata) {
            ctrl.metadata = metadata;
        });
    }
}());
