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

// TODO project calendar, fix day height and add missing arrows
    function CalendarController($sanitize, $state, $mdDateLocale, User, MaterialCalendarData) {
        var ctrl = this;

        ctrl.firstDayOfWeek = $mdDateLocale.firstDayOfWeek;

        ctrl.events = [];

        var syncCalendar = function () {
            for (var date in ctrl.events.cards) {

                var dayContent = '';

                for (var i = 0; i < ctrl.events.cards[date].length; i++) {
                    var card = ctrl.events.cards[date][i];

                    var cardHref = $state.href('calendar.card', {
                        projectName: card.projectShortName,
                        shortName: card.boardShortName,
                        seqNr: card.sequence
                    });

                    var cardTitle = $sanitize(card.boardShortName + '-' + card.sequence + ' ' + card.name);
                    var cardName = $sanitize(card.name);

                    dayContent += '<div class="lvg-calendar__day"><a href="' + cardHref + '" title="' + cardName + '">' + cardTitle + '</a></div>';
                }
                MaterialCalendarData.setDayContent(moment(date).toDate(), dayContent);
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
