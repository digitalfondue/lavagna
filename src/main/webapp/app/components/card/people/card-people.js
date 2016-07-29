(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardPeople', {
        bindings: {
            project: '<',
            card: '<',
            assignedUsers: '<',
            watchingUsers: '<',
            user: '<'
        },
        controller: CardPeopleController,
        templateUrl: 'app/components/card/people/card-people.html'
    });

    function CardPeopleController (BulkOperations, User) {
        var ctrl = this;

        var currentCard = function() {
            var cardByProject = {};
            cardByProject[ctrl.project.shortName] = [ctrl.card.id];
            return cardByProject;
        };

        ctrl.isWatching = function() {
            if(ctrl.watchingUsers === undefined) {
                return false;
            }

            for(var i = 0; i < ctrl.watchingUsers.length; i++) {
                if(ctrl.watchingUsers[i].value.valueUser === ctrl.user.id) {
                    return true;
                }
            }
            return false;
        }

        ctrl.watchCard = function() {
            BulkOperations.watch(currentCard(), ctrl.user);
        };

        ctrl.unWatchCard = function(user) {
            BulkOperations.unWatch(currentCard(), ctrl.user);
        };

        ctrl.searchUser = function(text) {
            return User.findUsers(text.trim()).then(function (res) {
                angular.forEach(res, function(user) {
                    user.label = User.formatName(user);
                });
                return res;
            });
        };

        ctrl.assignUser = function(user) {
            if(user === undefined || user === null) {
                return;
            }
            BulkOperations.assign(currentCard(), user);
        }

        ctrl.removeAssignForUser = function(user) {
            BulkOperations.removeAssign(currentCard(), {id: user.value.valueUser});
        };

    }
})();
