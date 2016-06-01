(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgUserDashboard', {
        bindings: {
            profile: '<'
        },
        controller: UserDashboardController,
        templateUrl: 'app/components/user/dashboard/dashboard.html'
    });

    function UserDashboardController($filter, User) {
        var ctrl = this;

        ctrl.view = {};

        var showCalHeatMap = function (dailyActivity) {

            var parser = function (data) {
                var stats = {};
                for (var d in data) {
                    stats[data[d].date / 1000] = data[d].count;
                }
                return stats;
            };

            var currentDate = new Date();
            var lastYear = new Date(currentDate.getFullYear(), currentDate.getMonth() - 11, 1);


            if (ctrl.cal) {
                ctrl.cal.update(dailyActivity, parser);
            } else {
                ctrl.cal = new CalHeatMap();
                ctrl.cal.init({
                    data: dailyActivity,
                    afterLoadData: parser,
                    domainDynamicDimension: true,
                    start: lastYear,
                    id: "graph_c",
                    domain: "month",
                    subDomain: "day",
                    range: 12,
                    cellPadding: 2,
                    itemName: ["event", "events"]
                });
            }
        };

        var groupByDate = function (events) {

            var groupedByDate = {};
            var keyOrder = [];

            for (var i in events) {
                var dateRepresentation = $filter('date')(events[i].time, 'dd.MM.yyyy');
                if (keyOrder.indexOf(dateRepresentation) == -1) {
                    keyOrder.push(dateRepresentation);
                    groupedByDate[dateRepresentation] = [];
                }

                groupedByDate[dateRepresentation].push(events[i]);
            }

            return {groupedByDate: groupedByDate, keyOrder: keyOrder};
        };

        var loadUser = function (profile) {
            ctrl.profile = profile;
            ctrl.user = profile.user;
            ctrl.activeProjects = profile.activeProjects;
        };

        ctrl.userProvider = ctrl.profile.user.provider;
        ctrl.userName = ctrl.profile.user.username;

        //init
        loadUser(ctrl.profile);

        showCalHeatMap(ctrl.profile.dailyActivity);

        User.getUserActivity(ctrl.profile.user.provider, ctrl.profile.user.username).then(function (activities) {
            ctrl.eventsGroupedByDate = groupByDate(activities);
        });
    }
})();
