(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgUserAvatar', {
        bindings: {
            userId: '<',
            size: '@',
            colorHex: '@',
            bgColorHex: '@'
        },
        controller: UserAvatarController,
        templateUrl: 'app/components/user-avatar/user-avatar.html'
    });

    function UserAvatarController(UserCache) {
        var ctrl = this;

        ctrl.size = ctrl.size || 28;
        ctrl.colorHex = ctrl.colorHex || '#333';
        ctrl.bgColorHex = ctrl.bgColorHex || '#e5e5e5';

        function loadUser() {
            UserCache.user(ctrl.userId).then(function (user) {
                ctrl.user = user;
            });
        }

        loadUser();
    };
})();
