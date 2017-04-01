(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgUserAvatar', {
        bindings: {
            userId: '&',
            size: '@',
            colorHex: '@',
            bgColorHex: '@'
        },
        controller: ['Tooltip', 'UserCache', '$element', UserAvatarController],
        template: '<div class="lvg-user-avatar"><span ng-if="::$ctrl.user">{{::($ctrl.user | userInitials)}}</span></div>'

    });

    function UserAvatarController(Tooltip, UserCache, $element) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            ctrl.size = ctrl.size || 28;
            ctrl.colorHex = ctrl.colorHex || '#333';
            ctrl.bgColorHex = ctrl.bgColorHex || '#e5e5e5';

            UserCache.user(ctrl.userId()).then(function (user) {
                ctrl.user = user;
            });
        };

        ctrl.$postLink = function postLink() {
            var innerSize = ctrl.size / 2; // we assume we always use even numbers

            $element.children().css({
                'width': ctrl.size + 'px',
                'height': ctrl.size + 'px',
                'color': ctrl.colorHex,
                'background-color': ctrl.bgColorHex,
                'border-radius': innerSize + 'px',
                'font-size': innerSize + 'px',
                'text-align': 'center',
                'line-height': ctrl.size + 'px'
            });

            $element[0].addEventListener('mouseenter', handleMouseEnter);
            $element[0].addEventListener('mouseleave', handleMouseLeave);
        };

        ctrl.$onDestroy = function onDestroy() {
            Tooltip.clean();

            $element[0].removeEventListener('mouseenter', handleMouseEnter);
            $element[0].removeEventListener('mouseleave', handleMouseLeave);
        };

        function handleMouseEnter($event) {
            Tooltip.user(ctrl.user, $event.target);
        }

        function handleMouseLeave() {
            Tooltip.clean();
        }
    }
}());
