(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCreateCard', {
        templateUrl: 'app/components/create-card/create-card.html',
        bindings: {
            projectShortName: '<',
            boardShortName: '<',
            projectMetadata: '<',
            column: '<',
            user: '<'
        },
        controller: ['$mdDialog', 'Board', 'Label', 'Notification', CreateCardController]
    });

    function CreateCardController($mdDialog, Board, Label, Notification) {
        var ctrl = this;

        ctrl.createAnother = false;

        ctrl.$onInit = function () {
            initData();
        };

        ctrl.onUpdateDescription = function ($name, $description, $labels) {
            ctrl.name = $name;
            ctrl.description = $description;
            ctrl.labels = $labels;
        };

        ctrl.onUpdateFiles = function ($files) {
            ctrl.files = $files;
        };

        ctrl.onUpdateMetadata = function ($column, $dueDate, $milestone) {
            ctrl.column = $column;
            ctrl.dueDate = $dueDate;
            ctrl.milestone = $milestone;
        };

        ctrl.onAddUser = function ($userId) {
            if (ctrl.assignedUsers.indexOf($userId) === -1) {
                ctrl.assignedUsers.push($userId);
            }
        };

        ctrl.onRemoveUser = function ($userId) {
            var removeIdx = ctrl.assignedUsers.indexOf($userId);

            if (removeIdx !== -1) {
                ctrl.assignedUsers.splice(removeIdx, 1);
            }
        };

        ctrl.createCard = function () {
            var assignedUsers = [];

            angular.forEach(ctrl.assignedUsers, function (userId) {
                assignedUsers.push({
                    value: Label.userVal(userId),
                    cardIds: []
                });
            });

            var cardToCreate = {
                name: ctrl.name,
                description: ctrl.description,
                labels: ctrl.labels,
                files: ctrl.files,
                dueDate: ctrl.dueDate === null ? ctrl.dueDate : {value: ctrl.dueDate, cardIds: []},
                milestone: ctrl.milestone === null ? ctrl.milestone : {value: ctrl.milestone, cardIds: []},
                assignedUsers: assignedUsers
            };

            Board.createCard(ctrl.column.id, cardToCreate).then(function (card) {
                if (ctrl.createAnother === true) {
                    Notification.addAutoAckNotification('success', {key: 'notification.card.create.success', parameters: {shortName: ctrl.boardShortName, sequence: card.sequence}}, false);

                    initData();
                } else {
                    close();
                }
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.card.create.error'}, false);
            });
        };

        ctrl.close = close;

        function initData() {
            ctrl.name = null;
            ctrl.description = null;
            ctrl.labels = [];
            ctrl.files = [];
            ctrl.dueDate = null;
            ctrl.milestone = null;
            ctrl.assignedUsers = [];
        }

        function close() {
            $mdDialog.hide();
        }
    }

})();
