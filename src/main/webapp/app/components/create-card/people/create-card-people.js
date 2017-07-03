(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCreateCardPeople', {
        bindings: {
            users: '<',
            user: '<',
            onAddUser: '&',
            onRemoveUser: '&'
        },
        controller: ['User', CreateCardPeopleController],
        templateUrl: 'app/components/create-card/people/create-card-people.html'
    });

    function CreateCardPeopleController(User) {
        var ctrl = this;

        ctrl.isAssigned = function () {
            return ctrl.users.indexOf(ctrl.user.id) !== -1;
        };

        ctrl.searchUser = function (text) {
            return User.findUsers(text.trim()).then(function (res) {
                angular.forEach(res, function (user) {
                    user.label = User.formatName(user);
                });

                return res;
            });
        };
    }
})();
