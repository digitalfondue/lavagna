(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCalendar', {
        bindings: {
            username: '<',
            provider: '<',
            project: '<'
        },
        templateUrl: 'app/components/calendar/calendar.html',
        controller: ['$rootScope', '$filter', '$sanitize', '$state', '$mdDateLocale', 'User', 'MaterialCalendarData', CalendarController]
    });

    function CalendarController($rootScope, $filter, $sanitize, $state, $mdDateLocale, User, MaterialCalendarData) {
        var ctrl = this;

        MaterialCalendarData.data = {};

        ctrl.firstDayOfWeek = $mdDateLocale.firstDayOfWeek;

        ctrl.events = [];

        var syncCalendar = function () {
            angular.forEach(ctrl.events.dailyEvents, function (dailyEvents, date) {
                var dayText = '';

                for (var i = 0; i < dailyEvents.milestones.length; i++) {
                    var m = dailyEvents.milestones[i];

                    var milestoneHref = $state.href('project.milestones.milestone', {
                        projectName: m.projectShortName,
                        id: m.label.id
                    });
                    var milestoneName = $sanitize(m.name);
                    var mClassTxt = '';

                    if (m.label.metadata && m.label.metadata.status === 'CLOSED') {
                        mClassTxt = 'class="lavagna-closed-milestone"';
                    }

                    dayText += '<div class="lvg-calendar__day"><a href="' + milestoneHref + '" title="' + milestoneName + '" ' + mClassTxt + '>' + milestoneName + '</a></div>';
                }

                var cards = $filter('orderBy')(dailyEvents.cards, ['-columnDefinition', 'boardShortName', 'sequence']);

                for (var j = 0; j < cards.length; j++) {
                    var card = cards[j];

                    var cardHref = $state.href(ctrl.project ? 'project.calendar.card' : 'calendar.card', {
                        projectName: card.projectShortName, shortName: card.boardShortName, seqNr: card.sequence
                    });
                    var cardTitle = $sanitize(card.boardShortName + '-' + card.sequence + ' ' + card.name);
                    var cardName = $sanitize(card.name);
                    var classTxt = '';

                    if (card.columnDefinition === 'CLOSED') {
                        classTxt = 'class="lavagna-closed-card"';
                    }

                    dayText += '<div class="lvg-calendar__day"><a href="' + cardHref + '" title="' + cardName + '" ' + classTxt + '>' + cardTitle + '</a></div>';
                }

                MaterialCalendarData.setDayContent(moment(date).toDate(), dayText);
            });
        };

        var getApiToCall = function () {
            if (ctrl.project) {
                return User.getProjectCalendar(ctrl.project.shortName);
            }

            return User.getCalendar();
        };

        var refreshEvents = function () {
            getApiToCall().then(function (events) {
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

        var unregStateChanges = $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState) {
            if (fromState.name === 'calendar.card' && toState.name === 'calendar') {
                refreshEvents();
            }
        });

        ctrl.$onDestroy = function onDestroy() {
            unregStateChanges();
            MaterialCalendarData.data = {};
        };
    }
}());
