(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCalendar', {
        bindings: {
            username: '<',
            provider: '<'
        },
        templateUrl: 'app/components/calendar/calendar.html',
        controller: ['$sanitize', '$state', '$mdDateLocale', 'User', 'MaterialCalendarData', CalendarController]
    });

    function CalendarController($sanitize, $state, $mdDateLocale, User, MaterialCalendarData) {
        var ctrl = this;

        ctrl.firstDayOfWeek = $mdDateLocale.firstDayOfWeek;

        ctrl.events = [];

        var syncCalendar = function () {


            for (var date in ctrl.events.dailyEvents) {

                var dayText = '';

                var dailyEvents = ctrl.events.dailyEvents[date];

                for (var i = 0; i < dailyEvents.milestones.length; i++) {
                    var m = dailyEvents.milestones[i];

                    var milestoneHref = $state.href('project.milestones.milestone', {
                        projectName: m.projectShortName,
                        id: m.label.id
                    });
                    var milestoneName = $sanitize(m.name);

                    dayText += '<div class="lvg-calendar__day"><a href="' + milestoneHref + '" title="' + milestoneName + '">' + milestoneName + '</a></div>';
                }

                for (var i = 0; i < dailyEvents.cards.length; i++) {
                    var card = dailyEvents.cards[i];

                    var cardHref = $state.href('calendar.card', {
                        projectName: card.projectShortName, shortName: card.boardShortName, seqNr: card.sequence
                    });
                    var cardTitle = $sanitize(card.boardShortName + '-' + card.sequence + ' ' + card.name);
                    var cardName = $sanitize(card.name);

                    dayText += '<div class="lvg-calendar__day"><a href="' + cardHref + '" title="' + cardName + '">' + cardTitle + '</a></div>';
                }

                MaterialCalendarData.setDayContent(moment(date).toDate(), dayText);
            }

        };

        var refreshEvents = function () {
            User.getCalendar().then(function (events) {
                ctrl.events = events;
                syncCalendar();
            });
        };

        refreshEvents();

        ctrl.prevMonth = function () {
            syncCalendar();
        };

        ctrl.nextMonth = function () {
            syncCalendar();
        };

    }
})();
