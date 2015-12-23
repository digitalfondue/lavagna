(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgComponentUserDashboard', UserDashboardComponent);

    function UserDashboardComponent() {
        return {
            restrict: 'E',
            scope: true,
            bindToController: {
                profile: '='
            },
            controller: UserDashboardController,
            controllerAs: 'userDashCtrl',
            templateUrl: 'app/components/user/dashboard/dashboard.html'
        }
    };

    function UserDashboardController() {
        var ctrl = this;

        ctrl.view = {};

        var showCalHeatMap = function (profile) {

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
                ctrl.cal.update(profile.dailyActivity, parser);
            } else {
                ctrl.cal = new CalHeatMap();
                ctrl.cal.init({
                    data: profile.dailyActivity,
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

        showCalHeatMap(ctrl.profile);
    };
})();
