(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCreateCard', {
        templateUrl: 'app/components/create-card/create-card.html',
        bindings: {
            project: '<',
            board: '<',
            user: '<'
        },
        controller: ['EventBus', 'CardCache', 'Card', 'LabelCache', 'Project', 'StompClient', 'Title', CreateCardController]
    });

    function CreateCardController(EventBus, CardCache, Card, LabelCache, Project, StompClient, Title) {
        var ctrl = this;
        var projectMetadataSubscription = angular.noop;

        ctrl.$onInit = function () {
            projectMetadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl.project);

            ctrl.card = initData();
        };

        ctrl.$onDestroy = function () {
            projectMetadataSubscription();
        };

        ctrl.onUpdateDescription = function ($title, $description, $labels) {
            ctrl.card.title = $title;
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

        function initData() {
            return {
                title: null,
                description: null,
                labels: [],
                files: [],
                columnId: null,
                dueDate: null,
                milestone: null,
                assignedUsers: [],
                actionLists: []
            };
        }
    }

})();
