(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCalendar', {
        bindings: {
            username: '<',
            provider: '<'
        },
        templateUrl: 'app/components/calendar/calendar.html',
        controller: ['$sanitize', 'User', 'MaterialCalendarData', CalendarController]
    });

// TODO sanitize html, project calendar, click to open a card, missing arrows
    function CalendarController($sanitize, User, MaterialCalendarData) {
        var ctrl = this;

        ctrl.firstDayOfWeek = 0;

        ctrl.events = [];

        var syncCalendar = function () {
            for (var i = 0; i < ctrl.events.length; i++) {
                var event = ctrl.events[i];
                MaterialCalendarData.setDayContent(moment(event.date).toDate(), '<span>' + event.name + '</span>')
            }
        };

        var refreshEvents = function () {
            User.getCalendar().then(function (events) {
                ctrl.events = events;
                syncCalendar();
            });
        };

        refreshEvents();

        ctrl.prevMonth = function (data) {
            syncCalendar();
        };

        ctrl.nextMonth = function (data) {
            syncCalendar();
        };

    }
})();
