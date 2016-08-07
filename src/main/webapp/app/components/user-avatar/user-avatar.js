(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgUserAvatar', {
        bindings: {
            userId: '<',
            size: '@'
        },
        controller: UserAvatarController,
        templateUrl: 'app/components/user-avatar/user-avatar.html'
    });

    function UserAvatarController(UserCache) {
        var ctrl = this;

        ctrl.size = ctrl.size || 28;

        function loadUser() {
            UserCache.user(ctrl.userId).then(function (user) {
                ctrl.user = user;
            });
        }

        loadUser();
    };
})();
