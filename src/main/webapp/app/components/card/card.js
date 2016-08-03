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
        controller: CardController
    });

    function CardController($scope, $rootScope, $timeout, CardCache, Card, User, LabelCache, Label, StompClient,
        Notification, Board, BulkOperations, Title) {
        var ctrl = this;
        var board = ctrl.board;
        var project = ctrl.project;
        var card = ctrl.card;

        ctrl.labels = ctrl.project.metadata.labels;

        ctrl.assignedUsers = [];
        ctrl.watchingUsers = [];
        ctrl.milestones = [];
        ctrl.dueDates = [];
        ctrl.userLabels = {};

        //------------------

        function refreshTitle() {
        	Title.set('title.card', { shortname: board.shortName, sequence: ctrl.card.sequence, name: ctrl.card.name });
        }

        var reloadCard = function() {
            CardCache.card(card.id).then(function(c) {
                ctrl.card = c;
                card = ctrl.card;
                refreshTitle();
            });
            loadActivity();
        };

        var unbindCardCache = $rootScope.$on('refreshCardCache-' + card.id, reloadCard);
        $scope.$on('$destroy', unbindCardCache);

        var loadComments = function() {
            Card.comments(card.id).then(function(comments) {
                ctrl.comments = comments;
            });
        };

        loadComments();

        //--------------

        ctrl.addComment = function(comment) {
            Card.addComment(card.id, comment).then(function() {
                comment.content = null;
            });
        };

        //
        ctrl.updateComment = function(comment, commentToEdit) {
            Card.updateComment(comment.id, commentToEdit);
        };
        ctrl.deleteComment = function(comment, control) {
            Card.deleteComment(comment.id).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.COMMENT_DELETE.success'}, true, true, function(notification) {
                    Card.undoDeleteComment(event.id).then(notification.acknowledge)
                });
            }, function(error) {
                control = false;
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
            });
        };

        var currentCard = function() {
            var cardByProject = {};
            cardByProject[project.shortName] = [card.id];
            return cardByProject;
        };

        //activity
        var loadActivity = function() {
            Card.activity(card.id).then(function(activities) {
                ctrl.activities = activities;
            });
        };

        loadActivity();

        ctrl.hasActivity = function() {
            return (ctrl.comments !== undefined && ctrl.actionLists !== undefined && ctrl.actionItems !== undefined && ctrl.files !== undefined);
        };


        // ----
        var loadLabel = function() {
            LabelCache.findByProjectShortName(project.shortName).then(function(labels) {
                ctrl.labels = labels;
            });
        };

        var unbind = $rootScope.$on('refreshLabelCache-' + project.shortName, loadLabel);
        $scope.$on('$destroy', unbind);

        var loadLabelValues = function() {
            Label.findValuesByCardId(card.id).then(function(labelValues) {
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
        loadLabelValues();

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe($scope, '/event/card/' + card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;
            if(type.match(/COMMENT/g) !== null) {
                loadComments();
                reloadCard();
            } else if(type.indexOf('LABEL') > -1) {
                loadLabelValues();
                reloadCard();
            }
        });
    }
})();
