(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgStatsPanelSimple', {
        templateUrl: 'app/components/stats/panel-simple/panel-simple.html',
        transclude: true,
        bindings: {
            item: '<',
            statsFetcher: '&'
        },
        controller: ['$filter', StatsPanelSimpleController]
    });

    function StatsPanelSimpleController($filter) {
        var ctrl = this;

        var colorFilter = $filter('color');

        ctrl.$onInit = function onInit() {
            ctrl.statsFetcher().then(function (stats) {
                if (stats === undefined) {
                    return;
                }
                ctrl.statistics = stats;

                if (hasTasks(stats)) {
                    ctrl.normalizedStats = createNormalizedData(stats);
                    ctrl.normalizedStatsBGColor = getBackGroundColor(stats);
                }
            });
        };

        function getBackGroundColor(stats) {
            if (stats.deferredTaskCount) {
                return colorFilter(stats.deferredTaskCount).color;
            } else if (stats.backlogTaskCount) {
                return colorFilter(stats.backlogTaskCount).color;
            } else if (stats.closedTaskCount) {
                return colorFilter(stats.closedTaskCount).color;
            } else {
                return colorFilter(stats.openTaskColor).color;
            }
        }

        function createNormalizedData(stats) {
            var normalizedStats = [];

            if (stats.openTaskCount) {
                normalizedStats.push({name: 'OPEN', value: stats.openTaskCount, percentage: stats.openTaskCount > 0 ? 1 : 0, color: colorFilter(stats.openTaskColor).color});
            }
            if (stats.closedTaskCount) {
                normalizedStats.push({name: 'CLOSED', value: stats.closedTaskCount, percentage: stats.closedTaskCount > 0 ? 1 : 0, color: colorFilter(stats.closedTaskColor).color});
            }
            if (stats.backlogTaskCount) {
                normalizedStats.push({name: 'BACKLOG', value: stats.backlogTaskCount, percentage: stats.backlogTaskCount > 0 ? 1 : 0, color: colorFilter(stats.backlogTaskColor).color});
            }
            if (stats.deferredTaskCount) {
                normalizedStats.push({name: 'DEFERRED', value: stats.deferredTaskCount, percentage: stats.deferredTaskCount > 0 ? 1 : 0, color: colorFilter(stats.deferredTaskColor).color});
            }

            var percentage = 100;
            var total = 0;

            angular.forEach(normalizedStats, function (stat) {
                percentage = percentage - stat.percentage;
                total += stat.value;
            });

            var usedPercentage = 0;
            var biggestPercentage = { index: null, value: 0 };

            angular.forEach(normalizedStats, function (stat, index) {
                if (stat.value > 0) {
                    stat.percentage += Math.floor((stat.value / total) * percentage);
                    usedPercentage += stat.percentage;
                    if (stat.percentage > biggestPercentage.value) {
                        biggestPercentage.value = stat.percentage;
                        biggestPercentage.index = index;
                    }
                }
            });

            if (usedPercentage < 100) {
                normalizedStats[biggestPercentage.index].percentage += 100 - usedPercentage;
            }

            return normalizedStats;
        }

        function hasTasks(stats) {
            var totalTasks = parseInt(stats.openTaskCount) +
                parseInt(stats.closedTaskCount) +
                parseInt(stats.backlogTaskCount) +
                parseInt(stats.deferredTaskCount);

            return totalTasks > 0;
        }
    }
}());
