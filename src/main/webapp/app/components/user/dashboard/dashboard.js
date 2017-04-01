(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgUserDashboard', {
        bindings: {
            profile: '<'
        },
        controller: ['$filter', '$translate', 'Project', 'User', UserDashboardController],
        templateUrl: 'app/components/user/dashboard/dashboard.html'
    });

    function UserDashboardController($filter, $translate, Project, User) {
        var ctrl = this;

        ctrl.activityChartOptions = {
            pointDot: false,
            bezierCurve: true,
            scaleIntegersOnly: true,
            showTooltips: true,
            scaleBeginAtZero: true,
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            scaleOverride: true,
            scaleSteps: 3,
            scaleStartValue: 0
        };
        var color = '#B0BEC5';

        ctrl.$onInit = function init() {
            ctrl.view = {};
            ctrl.userProvider = ctrl.profile.user.provider;
            ctrl.userName = ctrl.profile.user.username;

            // init
            loadUser(ctrl.profile);

            User.getUserActivity(ctrl.profile.user.provider, ctrl.profile.user.username).then(function (activities) {
                ctrl.eventsGroupedByDate = groupByDate(activities);
            });

            ctrl.activityChartData = getActivityChartData(ctrl.profile.dailyActivity);
        };

        function getActivityChartData(events) {
            var eventsByMonth = groupByMonth(events);

            var chartData = { labels: [], datasets: []};

            chartData.datasets.push({
                strokeColor: color, fillColor: color, pointColor: color, spanGaps: true,
                data: []
            });

            var locale = $translate.use();

            for (var i = 11; i >= 0; i--) {
                var date = new Date();

                date.setMonth(date.getMonth() - i);
                chartData.labels.push(date.toLocaleString(locale, { month: 'short' }));
                chartData.datasets[0].data.push(eventsByMonth[date.getMonth()]);
            }

            var maxValue = Math.max.apply(Math, chartData.datasets[0].data);

            ctrl.activityChartOptions.scaleStepWidth = maxValue > 3 ? Math.ceil(Math.max(maxValue) / 3) : 1;

            return chartData;
        }

        function groupByMonth(events) {
            var eventsByMonth = [];

            for (var i = 0; i < 12; i++) {
                eventsByMonth[i] = 0;
            }

            angular.forEach(events, function (event) {
                var date = new Date(event.date);

                eventsByMonth[date.getMonth()] += event.count;
            });

            return eventsByMonth;
        }

        function groupByDate(events) {
            var groupedByDate = {};
            var keyOrder = [];

            angular.forEach(events, function (event) {
                var dateRepresentation = $filter('date')(event.time, 'dd.MM.yyyy');

                if (keyOrder.indexOf(dateRepresentation) === -1) {
                    keyOrder.push(dateRepresentation);
                    groupedByDate[dateRepresentation] = [];
                }

                groupedByDate[dateRepresentation].push(event);
            });

            return {groupedByDate: groupedByDate, keyOrder: keyOrder};
        }

        function loadUser(profile) {
            ctrl.profile = profile;
            ctrl.user = profile.user;
            ctrl.activeProjects = profile.activeProjects;
            var grid = Project.gridByDescription(profile.activeProjects, true);

            ctrl.activeProjectsLeft = grid.left;
            ctrl.activeProjectsRight = grid.right;
        }
    }
}());
