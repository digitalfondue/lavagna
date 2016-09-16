(function() {

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
        controller: ['EventBus', 'CardCache', 'Card', 'LabelCache', 'Label', 'Project', 'StompClient', 'Title', CardController]
    });

    function CardController(EventBus, CardCache, Card, LabelCache, Label, Project, StompClient, Title) {
        var ctrl = this;

        var unbindCardCache = angular.noop;
        var unbindLabelCache = angular.noop;
        var unbindStomp = angular.noop;

        var projectMetadataSubscription;

        ctrl.$onInit = function() {

        	ctrl.labels = ctrl.project.metadata.labels;
            ctrl.assignedUsers = [];
            ctrl.watchingUsers = [];
            ctrl.milestones = [];
            ctrl.dueDates = [];
            ctrl.userLabels = {};

            unbindCardCache = EventBus.on('refreshCardCache-' + ctrl.card.id, reloadCard);

            unbindLabelCache = EventBus.on('refreshLabelCache-' + ctrl.project.shortName, loadLabel);

            //the /card-data has various card data related event that are pushed from the server that we must react
            unbindStomp = StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function(e) {
                var type = JSON.parse(e.body).type;
                if(type.indexOf('LABEL') > -1) {
                    loadLabelValues();
                    reloadCard();
                }
            });

            loadLabelValues();

            projectMetadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl.project);
        }

        ctrl.$onDestroy = function onDestroy() {
        	unbindCardCache();
        	unbindLabelCache();
        	unbindStomp();
        	projectMetadataSubscription();
        }



        //------------------

        function refreshTitle() {
        	Title.set('title.card', { shortname: ctrl.board.shortName, sequence: ctrl.card.sequence, name: ctrl.card.name });
        }

        function reloadCard() {
            CardCache.card(ctrl.card.id).then(function(c) {
                ctrl.card = c;
                refreshTitle();
            });
        };

        function currentCard() {
            var cardByProject = {};
            cardByProject[ctrl.project.shortName] = [ctrl.card.id];
            return cardByProject;
        };

        // ----
        function loadLabel() {
            LabelCache.findByProjectShortName(ctrl.project.shortName).then(function(labels) {
                ctrl.labels = labels;
            });
        };

        function loadLabelValues() {
            Label.findValuesByCardId(ctrl.card.id).then(function(labelValues) {
                ctrl.labelValues = labelValues;

                angular.forEach(ctrl.labels, function(value, key) {
                    if(value.domain === 'SYSTEM') {
                        if(value.name === 'ASSIGNED') {
                            ctrl.assignedUsers = ctrl.labelValues[key];
                        } else if(value.name === 'WATCHED_BY') {
                            ctrl.watchingUsers = ctrl.labelValues[key];
                        } else if(value.name === 'MILESTONE') {
                            ctrl.milestones = ctrl.labelValues[key];
                        } else if(value.name === 'DUE_DATE') {
                            ctrl.dueDates = ctrl.labelValues[key];
                        }
                    } else {
                        ctrl.userLabels[key] = value;
                    }
                });
            });
        };
    }
})();
