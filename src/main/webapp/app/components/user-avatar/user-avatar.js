(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgUserAvatar', {
        bindings: {
            userId: '<',
            size: '@',
            colorHex: '@',
            bgColorHex: '@'
        },
        controller: ['UserCache', UserAvatarController],
        template: '<ng-avatar ng-if="::$ctrl.user" width="{{::$ctrl.size}}" class="lvg-user-avatar" bg-color="{{::$ctrl.bgColorHex}}" '
            			+' color="{{::$ctrl.colorHex}}" round-shape="true" initials="{{::($ctrl.user | userInitials)}}">'
            			+'</ng-avatar>'

    });

    function UserAvatarController(UserCache) {
        var ctrl = this;
        
        ctrl.$onInit = function init() {
        	ctrl.size = ctrl.size || 28;
            ctrl.colorHex = ctrl.colorHex || '#333';
            ctrl.bgColorHex = ctrl.bgColorHex || '#e5e5e5';
            
            UserCache.user(ctrl.userId).then(function (user) {
                ctrl.user = user;
            });
        }
    };
})();
