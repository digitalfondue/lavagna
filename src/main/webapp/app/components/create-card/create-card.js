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
        controller: ['$mdDialog', 'Board', 'Notification', CreateCardController]
    });

    function CreateCardController($mdDialog, Board, Notification) {
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

        ctrl.onUploaded = function ($file) {
            ctrl.files.push($file);
        };

        ctrl.onUpdateMetadata = function ($column, $dueDate, $milestone) {
            ctrl.column = $column;
            ctrl.dueDate = $dueDate;
            ctrl.milestone = $milestone;
        };

        ctrl.onUpdateUsers = function ($users) {
            ctrl.assignedUsers = $users;
        };

        ctrl.createCard = function () {
            var cardToCreate = {
                name: ctrl.name,
                description: ctrl.description,
                labels: ctrl.labels,
                files: ctrl.files,
                dueDate: ctrl.dueDate === null ? ctrl.dueDate : {value: ctrl.dueDate, cardIds: []},
                milestone: ctrl.milestone === null ? ctrl.milestone : {value: ctrl.milestone, cardIds: []},
                assignedUsers: ctrl.assignedUsers
            };

            Board.createCard(ctrl.column.id, cardToCreate).then(function (card) {
                if (ctrl.createAnother === true) {
                    Notification.addAutoAckNotification('success', {key: 'notification.card.create.success'}, false);

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
