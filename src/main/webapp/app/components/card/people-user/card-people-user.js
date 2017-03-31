(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardPeopleUser', {
        bindings: {
            userId: '<'
        },
        controller: function () {},
        template: '<div class="lvg-card-people-user row row-inherit start-xs middle-xs">'
                    + '<div class="lvg-card-people-user__avatar flex no-grow">'
                    + '<lvg-user-avatar user-id="$ctrl.userId" size="28"></lvg-user-avatar>'
                    + '</div>'
                    + '<div class="lvg-card-people-user__link flex">'
                    + '<lvg-user-link user-id="$ctrl.userId" class="flex"></lvg-user-link>'
                    + '</div></div>'
    });
}());
