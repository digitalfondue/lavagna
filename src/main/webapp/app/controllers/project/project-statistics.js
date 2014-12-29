(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectStatisticsCtrl', function ($stateParams, $scope, $translate, $filter, Project, Board, project //resolved by ui-route
		) {

		$scope.sidebarOpen = true;
		$scope.project = project;


		$scope.showCreatedAndClosedCards = false;
		$scope.showCardsHistory = false;
		$scope.showCardsByLabel = false;

		$scope.cardsHistoryChartOptions = {
			pointDot: false,
			bezierCurve: false,
			scaleIntegersOnly: true,
			showTooltips: false,
			scaleBeginAtZero: true,
			responsive: true,
			maintainAspectRatio: false
		};

		$scope.cardsByLabelChartOptions = {
			responsive: true,
			maintainAspectRatio: false
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

			$scope.stats = stats;

			$scope.totalCards = stats.openTaskCount + stats.closedTaskCount + stats.deferredTaskCount + stats.backlogTaskCount;

			$scope.chartCardsHistoryData = cardsHistory;
			$scope.showCardsHistory = cardsHistory.labels.length > 1;

			// cards by label
			$scope.cardsByLabelMax = 1;
			for (var i = 0; i < stats.cardsByLabel.length; i++) {
				var label = stats.cardsByLabel[i];
				$scope.cardsByLabelMax = Math.max($scope.cardsByLabelMax, label.count);
			}
			$scope.showCardsByLabel = stats.cardsByLabel.length > 0;

			// created vs closed
			$scope.openedThisPeriod = 0;
			$scope.closedThisPeriod = 0;

			for (var index in stats.createdAndClosedCards) {
				var pair = stats.createdAndClosedCards[index];
				$scope.openedThisPeriod += pair['first'];
				$scope.closedThisPeriod += pair['second'];
			}
			$scope.showCreatedAndClosedCards = Object.keys(stats.createdAndClosedCards).length > 1;
		};

		$scope.availableDateRanges = [];

		$translate('partials.project.statistics.last30Days').then(function (translatedValue) {
			$scope.availableDateRanges.push({name: translatedValue, value: moment().day(-30).toDate()});
		});
		$translate('partials.project.statistics.last14Days').then(function (translatedValue) {
			var range = {name: translatedValue, value: moment().day(-14).toDate()};
			$scope.availableDateRanges.push(range);
			$scope.dateRange = range;
		});
		$translate('partials.project.statistics.last7Days').then(function (translatedValue) {
			$scope.availableDateRanges.push({name: translatedValue, value: moment().day(-7).toDate()});
		});
		$translate('partials.project.statistics.thisMonth').then(function (translatedValue) {
			$scope.availableDateRanges.push({name: translatedValue, value: moment().startOf('month').toDate()});
		});
		$translate('partials.project.statistics.thisWeek').then(function (translatedValue) {
			$scope.availableDateRanges.push({name: translatedValue, value: moment().startOf('week').toDate()});
		});

		$scope.filterByBoard = function (board) {
			if ($scope.boards[0] == board) {
				Project.statistics(project.shortName, $scope.dateRange.value).then(generateDashboard);
			} else {
				Board.statistics(board.shortName, $scope.dateRange.value).then(generateDashboard);
			}
		};

		$scope.changeDateRange = function (range) {
			$scope.dateRange = range;
			$scope.filterByBoard($scope.boardToFilter);
		};

		Project.findBoardsInProject(project.shortName).then(function (boards) {
			boards.unshift({name: "All", archived: false});
			$scope.boards = boards;
			$scope.boardToFilter = boards[0];
			$scope.filterByBoard($scope.boardToFilter);
		});
	});
})();

