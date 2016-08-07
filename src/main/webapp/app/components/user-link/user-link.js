(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgUserLink', {
        bindings: {
            userId: '<'
        },
        controller: UserLinkController,
        templateUrl: 'app/components/user-link/user-link.html'
    });

    function UserLinkController(UserCache) {
        var ctrl = this;

        function loadUser() {
            UserCache.user(ctrl.userId).then(function (user) {
                ctrl.user = user;
            });
        }

        loadUser();
    };
})();
