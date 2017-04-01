/* eslint indent: 0 */
(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgUserLink', {
        bindings: {
            userId: '&'
        },
        template: '<a ng-if="::$ctrl.user" class="lvg-user-link" '
                      + ' ui-sref="user.dashboard(::({provider: $ctrl.user.provider, username: $ctrl.user.username}))" '
                    + ' data-ng-class="::{\'lvg-user-link__disabled\': !$ctrl.user.enabled}">'
                    + '{{::($ctrl.user | formatUser)}}'
                  + '</a>',
        controller: ['Tooltip', 'UserCache', '$element', UserLinkController]
    });

    function UserLinkController(Tooltip, UserCache, $element) {
        var ctrl = this;

        ctrl.$onInit = loadUser;

        function loadUser() {
            UserCache.user(ctrl.userId()).then(function (user) {
                ctrl.user = user;
            });
        }

        ctrl.$postLink = function postLink() {
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
