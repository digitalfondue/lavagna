(function () {
    'use strict';

    angular
        .module('lavagna.components')
        .component('lvgDialogSelectUser', {
            templateUrl: 'app/components/dialog/select-user/dialog-select-user.html',
            bindings: {
                dialogTitle: '<',
                action: '&' // $user
            },
            controller: ['$mdDialog', 'User', DialogSelectUserController]
        });

    function DialogSelectUserController($mdDialog, User) {
        var ctrl = this;

        ctrl.searchUser = searchUser;
        ctrl.cancel = cancel;
        ctrl.ok = ok;

        function cancel() {
            $mdDialog.hide();
        }

        function ok(user) {
            ctrl.action({'$user': user});
            $mdDialog.hide();
        }

        function searchUser(text) {
            return User.findUsers(text.trim()).then(function (res) {
                angular.forEach(res, function (user) {
                    user.label = User.formatName(user);
                });

                return res;
            });
        }
    }
}());
