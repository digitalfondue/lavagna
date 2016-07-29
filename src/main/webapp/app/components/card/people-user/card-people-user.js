(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardPeopleUser', {
        bindings: {
            userId: '<'
        },
        controller: UserLinkController,
        templateUrl: 'app/components/card/people-user/card-people-user.html'
    });

    function UserLinkController(UserCache) {
        var ctrl = this;

        function loadUser() {
            UserCache.user(ctrl.userId).then(function (user) {
                ctrl.user = user;
            });
        }

        loadUser();
    }
})();
