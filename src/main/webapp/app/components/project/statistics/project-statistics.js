(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgProjectStatistics', {
        controller: ProjectStatisticsController,
        controllerAs: 'projectStatsCtrl',
        bindings: {
            project: '='
        },
        templateUrl: 'app/components/project/statistics/project-statistics.html'
    });

	function ProjectStatisticsController($translate, $filter, Project, Board) {
	    var projectStatsCtrl = this;

        projectStatsCtrl.showCreatedAndClosedCards = false;
        projectStatsCtrl.showCardsHistory = false;
        projectStatsCtrl.showCardsByLabel = false;

        projectStatsCtrl.cardsHistoryChartOptions = {
            pointDot: false,
            bezierCurve: false,
            scaleIntegersOnly: true,
            showTooltips: false,
            scaleBeginAtZero: true,
            responsive: true,
            maintainAspectRatio: false,
            animation : false,
            scaleOverride : true,
            scaleSteps : 4,
            scaleStartValue : 0
        };

        projectStatsCtrl.cardsByLabelChartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            animation : false
        };

        var generateDashboard = function (stats) {
            var cardsHistory = {
                labels: [],
                datasets: []
            };

            function getSortedIndexes(array) {
                var sortedIndexes = [];
                for (var index in array) {
                    sortedIndexes.push(parseInt(index));
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

            createCardsHistorySeries("Backlog", $filter('color')(stats.backlogTaskColor).color);
            createCardsHistorySeries("Open", $filter('color')(stats.openTaskColor).color);
            createCardsHistorySeries("Deferred", $filter('color')(stats.deferredTaskColor).color);
            createCardsHistorySeries("Closed", $filter('color')(stats.closedTaskColor).color);


            var sortedHistoryIndexes = getSortedIndexes(stats.statusHistory);

            // cards history
            for (var i = 0; i < sortedHistoryIndexes.length; i++) {
                var index = sortedHistoryIndexes[i];
                cardsHistory.labels.push(new Date(index).toLocaleDateString());

                var closed = getStatusFromHistory(stats, index, "CLOSED");
                cardsHistory.datasets[3].data.push(closed);

                var deferred = closed + getStatusFromHistory(stats, index, "DEFERRED");
                cardsHistory.datasets[2].data.push(deferred);

                var open = deferred + getStatusFromHistory(stats, index, "OPEN");
                cardsHistory.datasets[1].data.push(open);

                var backlog = open + getStatusFromHistory(stats, index, "BACKLOG");
                cardsHistory.datasets[0].data.push(backlog);
            }

            //scale for the chart
            var maxValue = Math.max.apply(Math, cardsHistory.datasets[0].data);
            projectStatsCtrl.cardsHistoryChartOptions.scaleStepWidth = maxValue > 4 ? Math.ceil(Math.max(maxValue) / 4) : 1;

            projectStatsCtrl.stats = stats;

            projectStatsCtrl.totalCards = stats.openTaskCount + stats.closedTaskCount + stats.deferredTaskCount + stats.backlogTaskCount;

            projectStatsCtrl.chartCardsHistoryData = cardsHistory;
            projectStatsCtrl.showCardsHistory = cardsHistory.labels.length > 1;

            // cards by label
            projectStatsCtrl.cardsByLabelMax = 1;
            for (var i = 0; i < stats.cardsByLabel.length; i++) {
                var label = stats.cardsByLabel[i];
                projectStatsCtrl.cardsByLabelMax = Math.max(projectStatsCtrl.cardsByLabelMax, label.count);
            }
            projectStatsCtrl.showCardsByLabel = stats.cardsByLabel.length > 0;

            // created vs closed
            projectStatsCtrl.openedThisPeriod = 0;
            projectStatsCtrl.closedThisPeriod = 0;

            for (var index in stats.createdAndClosedCards) {
                var pair = stats.createdAndClosedCards[index];
                projectStatsCtrl.openedThisPeriod += pair['first'];
                projectStatsCtrl.closedThisPeriod += pair['second'];
            }
            projectStatsCtrl.showCreatedAndClosedCards = Object.keys(stats.createdAndClosedCards).length > 1;
        };

        projectStatsCtrl.availableDateRanges = [];

        $translate('partials.project.statistics.last30Days').then(function (translatedValue) {
            projectStatsCtrl.availableDateRanges.push({name: translatedValue, value: moment().day(-30).toDate()});
        });
        $translate('partials.project.statistics.last14Days').then(function (translatedValue) {
            var range = {name: translatedValue, value: moment().day(-14).toDate()};
            projectStatsCtrl.availableDateRanges.push(range);
            projectStatsCtrl.dateRange = range;
        });
        $translate('partials.project.statistics.last7Days').then(function (translatedValue) {
            projectStatsCtrl.availableDateRanges.push({name: translatedValue, value: moment().day(-7).toDate()});
        });
        $translate('partials.project.statistics.thisMonth').then(function (translatedValue) {
            projectStatsCtrl.availableDateRanges.push({name: translatedValue, value: moment().startOf('month').toDate()});
        });
        $translate('partials.project.statistics.thisWeek').then(function (translatedValue) {
            projectStatsCtrl.availableDateRanges.push({name: translatedValue, value: moment().startOf('week').toDate()});
        });

        projectStatsCtrl.filterByBoard = function (board) {
            if (projectStatsCtrl.boards[0] == board) {
                Project.statistics(projectStatsCtrl.project.shortName, projectStatsCtrl.dateRange.value).then(generateDashboard);
            } else {
                Board.statistics(board.shortName, projectStatsCtrl.dateRange.value).then(generateDashboard);
            }
        };

        projectStatsCtrl.changeDateRange = function (range) {
            projectStatsCtrl.dateRange = range;
            projectStatsCtrl.filterByBoard(projectStatsCtrl.boardToFilter);
        };

        Project.findBoardsInProject(projectStatsCtrl.project.shortName).then(function (boards) {
            boards.unshift({name: "All", archived: false});
            projectStatsCtrl.boards = boards;
            projectStatsCtrl.boardToFilter = boards[0];
            projectStatsCtrl.filterByBoard(projectStatsCtrl.boardToFilter);
        });
	}

})();
