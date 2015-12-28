(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentUser', {
        bindings: {
            profile: '=user'
        },
        controller: UserController,
        controllerAs: 'userCtrl',
        templateUrl: 'app/components/user/user.html'
    });

    function UserController($filter, User) {
        var ctrl = this;
        

        ctrl.view = {};

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

            ctrl.profile.latestActivity20 = profile.latestActivity.slice(0, 20);
            ctrl.profile.eventsGroupedByDate = groupByDate(ctrl.profile.latestActivity20);
            return profile;
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
    };
})();
