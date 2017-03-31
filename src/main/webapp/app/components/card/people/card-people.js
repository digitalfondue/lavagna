(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardPeople', {
        bindings: {
            project: '&',
            card: '&',
            assignedUsers: '<',
            watchingUsers: '<',
            user: '&'
        },
        controller: CardPeopleController,
        templateUrl: 'app/components/card/people/card-people.html'
    });

    function CardPeopleController(BulkOperations, User) {
        var ctrl = this;

        var card = ctrl.card();
        var project = ctrl.project();
        var user = ctrl.user();

        var currentCard = function () {
            var cardByProject = {};

            cardByProject[project.shortName] = [card.id];

            return cardByProject;
        };

        function isUserInList(list, user) {
            if (list === undefined) {
                return false;
            }

            for (var i = 0; i < list.length; i++) {
                if (list[i].value.valueUser === user.id) {
                    return true;
                }
            }

            return false;
        }

        ctrl.isWatching = function () {
            return isUserInList(ctrl.watchingUsers, user);
        };

        ctrl.isAssigned = function () {
            return isUserInList(ctrl.assignedUsers, user);
        };

        ctrl.watchCard = function () {
            BulkOperations.watch(currentCard(), user);
        };

        ctrl.unWatchCard = function () {
            BulkOperations.unWatch(currentCard(), user);
        };

        ctrl.take = function () {
            BulkOperations.assign(currentCard(), user);
        };

        ctrl.surrender = function () {
            BulkOperations.removeAssign(currentCard(), {id: user.id});
        };

        ctrl.searchUser = function (text) {
            return User.findUsers(text.trim()).then(function (res) {
                angular.forEach(res, function (user) {
                    user.label = User.formatName(user);
                });

                return res;
            });
        };

        ctrl.assignUser = function (user) {
            if (user === undefined || user === null) {
                return;
            }
            BulkOperations.assign(currentCard(), user);
        };

        ctrl.removeAssignForUser = function (user) {
            BulkOperations.removeAssign(currentCard(), {id: user.value.valueUser});
        };
    }
}());
