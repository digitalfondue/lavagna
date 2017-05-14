(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCreateCard', {
        templateUrl: 'app/components/create-card/create-card.html',
        bindings: {
            projectShortName: '<',
            boardShortName: '<',
            projectMetadata: '<',
            columns: '<',
            column: '<',
            user: '<'
        },
        controller: ['$mdDialog', 'Board', 'Card', 'LabelCache', 'Notification', 'Project', CreateCardController]
    });

    function CreateCardController($mdDialog, Board, Card, LabelCache, Notification) {
        var ctrl = this;

        ctrl.createAnother = false;

        ctrl.$onInit = function () {
            ctrl.card = initData();
            ctrl.card.column = ctrl.column;
        };

        ctrl.onUpdateDescription = function ($name, $description, $labels) {
            ctrl.card.name = $name;
            ctrl.card.description = $description;
            ctrl.card.labels = $labels;
        };

        ctrl.onUploaded = function ($file) {
            ctrl.card.files.push($file);
        };

        ctrl.onUpdateMetadata = function ($columnId, $dueDate, $milestone) {
            ctrl.card.columnId = $columnId;
            ctrl.card.dueDate = $dueDate;
            ctrl.card.milestone = $milestone;
        };

        ctrl.onUpdateUsers = function ($users) {
            ctrl.card.users = $users;
        };

        ctrl.onUpdateActionLists = function ($actionLists) {
            ctrl.card.actionLists = $actionLists;
        };

        ctrl.createCard = function () {
            Board.createCard(ctrl.card.column.id, ctrl.card).then(function () {
                if (ctrl.createAnother === true) {
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
            return {
                name: null,
                description: null,
                labels: [],
                files: [],
                column: null,
                dueDate: null,
                milestone: null,
                assignedUsers: [],
                actionLists: []
            };
        }

        function close() {
            $mdDialog.hide();
        }
    }

})();
