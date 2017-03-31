(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgUserTooltip', {
        bindings: {
            userRef: '&'
        },
        controller: UserTooltipController,
        templateUrl: 'app/components/user-tooltip/user-tooltip.html'
    });

    function UserTooltipController() {
        var ctrl = this;

        ctrl.$onInit = function onInit() {
            ctrl.user = ctrl.userRef();
        };
    }
}());
