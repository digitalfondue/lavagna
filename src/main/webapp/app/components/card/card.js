(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentCard', {
        templateUrl: 'app/components/card/card.html',
        bindings: {
            project: '<',
            board: '<',
            card: '<',
            user: '<'
        },
        controller: ['EventBus', 'CardCache', 'LabelCache', 'Project', 'StompClient', 'Title', CardController]
    });

    function CardController(EventBus, CardCache, LabelCache, Project, StompClient, Title) {
        var ctrl = this;

        var unbindCardCache = angular.noop;
        var unbindLabelCache = angular.noop;
        var unbindStomp = angular.noop;

        var projectMetadataSubscription;

        ctrl.$onInit = function () {
            ctrl.labels = ctrl.project.metadata.labels;
            ctrl.assignedUsers = [];
            ctrl.watchingUsers = [];
            ctrl.milestones = [];
            ctrl.dueDates = [];
            ctrl.userLabels = {};

            unbindCardCache = EventBus.on('refreshCardCache-' + ctrl.card.id, reloadCard);

            unbindLabelCache = EventBus.on('refreshLabelCache-' + ctrl.project.shortName, loadLabel);

            // the /card-data has various card data related event that are pushed from the server that we must react
            unbindStomp = StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function (e) {
                var type = JSON.parse(e.body).type;

                if (type.indexOf('LABEL') > -1) {
                    reloadCard(true);
                }
            });

            processCardLabels();

            projectMetadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl.project);
        };

        ctrl.$onDestroy = function onDestroy() {
            unbindCardCache();
            unbindLabelCache();
            unbindStomp();
            projectMetadataSubscription();
        };

        // ------------------

        function refreshTitle() {
            Title.set('title.card', { shortname: ctrl.board.shortName, sequence: ctrl.card.sequence, name: ctrl.card.name });
        }

        function reloadCard(forceReload) {
            CardCache.card(ctrl.card.id, forceReload).then(function (c) {
                ctrl.card = c;
                refreshTitle();
                processCardLabels();
            });
        }

        // ----
        function loadLabel() {
            LabelCache.findByProjectShortName(ctrl.project.shortName).then(function (labels) {
                ctrl.labels = labels;
            });
        }

        function processCardLabels() {
            var assignedUsers = [];
            var watchingUsers = [];
            var milestones = [];
            var dueDates = [];
            var userLabels = [];

            angular.forEach(ctrl.card.labels, function (label) {
                if (label.labelDomain === 'SYSTEM') {
                    if (label.labelName === 'ASSIGNED') {
                        assignedUsers.push(label);
                    } else if (label.labelName === 'WATCHED_BY') {
                        watchingUsers.push(label);
                    } else if (label.labelName === 'MILESTONE') {
                        milestones.push(label);
                    } else if (label.labelName === 'DUE_DATE') {
                        dueDates.push(label);
                    }
                } else {
                    if (userLabels[label.labelId] === undefined) {
                        userLabels[label.labelId] = [];
                    }
                    userLabels[label.labelId].push(label);
                }
            });

            ctrl.assignedUsers = assignedUsers;
            ctrl.watchingUsers = watchingUsers;
            ctrl.milestones = milestones;
            ctrl.dueDates = dueDates;
            ctrl.userLabels = userLabels;
        }
    }
}());
