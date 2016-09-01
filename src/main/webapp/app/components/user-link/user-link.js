(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgUserLink', {
        bindings: {
            userId: '<'
        },
        controller: UserLinkController,
        template: '<a ng-if="::$ctrl.user" class="lvg-user-link" '
        	      	+ ' ui-sref="user.dashboard({provider: $ctrl.user.provider, username: $ctrl.user.username})" '
        		    + ' data-ng-class="::{\'lvg-user-link__disabled\': !$ctrl.user.enabled}">'
        		    + '{{::($ctrl.user | formatUser)}}'
        		  +'</a>'
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
