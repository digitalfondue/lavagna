(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentUserDashboard', {
        bindings: {
            profile: '='
        },
        controller: UserDashboardController,
        controllerAs: 'userDashCtrl',
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

            ctrl.hasMore = profile.latestActivity.length > 20;
            ctrl.activeProjects = profile.activeProjects;

            ctrl.latestActivity20 = profile.latestActivity.slice(0, 20);
            ctrl.eventsGroupedByDate = groupByDate(ctrl.latestActivity20);
        };

        ctrl.userProvider = ctrl.profile.user.provider;
        ctrl.userName = ctrl.profile.user.username;

        ctrl.page = 0;

        ctrl.loadFor = function (page) {
            User.getUserProfile(ctrl.userProvider, ctrl.userName, page)
                .then(function (profile) {
                    return loadUser(profile);
                })
                .then(function (profile) {
                    showCalHeatMap(profile)
                }).then(function () {
                    ctrl.page = page
                });
        };

        //init
        loadUser(ctrl.profile);

        showCalHeatMap(ctrl.profile.dailyActivity);
    };
})();
