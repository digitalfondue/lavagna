(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardMetadata', {
        bindings: {
            card: '<',
            board: '<',
            project: '<',
            user: '<'
        },
        controller: CardMetadataController,
        templateUrl: 'app/components/card/metadata/card-metadata.html'
    });

    var COMPONENT_PERMISSIONS = ['UPDATE_CARD'];

    function CardMetadataController($rootScope, $scope, CardCache, Card, User, LabelCache, Label, StompClient,
        Notification, Board, BulkOperations, Search) {
        var ctrl = this;

        // return the current card in a bulk operation friendly data structure
        var currentCard = function() {
            var cardByProject = {};
            cardByProject[ctrl.project.shortName] = [ctrl.card.id];
            return cardByProject;
        };

        // -----
        ctrl.updateCardName = function(newName) {
            Card.update(ctrl.card.id, newName).then( function() {
                $rootScope.$emit('card.renamed.event');
            });
        };

        // -----
        var loadDescription = function() {
            Card.description(ctrl.card.id).then(function(description) {
                ctrl.description = description;
            });
        };

        loadDescription();

        ctrl.labelNameToId = {};

        var loadLabel = function() {
            LabelCache.findByProjectShortName(ctrl.project.shortName).then(function(labels) {
                ctrl.labels = labels;
                ctrl.userLabels = {};
                ctrl.labelNameToId = {};
                for(var k in labels) {
                    ctrl.labelNameToId[labels[k].name] = k;
                    if(labels[k].domain === 'USER') {
                        ctrl.userLabels[k] = labels[k];
                    }
                }
            });
        };
        loadLabel();

        var unbind = $rootScope.$on('refreshLabelCache-' + ctrl.project.shortName, loadLabel);
        $scope.$on('$destroy', unbind);

        var loadLabelValues = function() {
            Label.findValuesByCardId(ctrl.card.id).then(function(labelValues) {
                ctrl.labelValues = labelValues;
            });
        };
        loadLabelValues();

        ctrl.updateDescription = function(description) {
            Card.updateDescription(ctrl.card.id, description);
        };

        var loadColumn = function(columnId) {
            Board.column(columnId).then(function(col) {
                ctrl.column = col;
            });
        };
        loadColumn(ctrl.card.columnId);

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

        ctrl.minDueDate = new Date();

        ctrl.setDueDate = function(date) {
            BulkOperations.setDueDate(currentCard(), date)
        };

        ctrl.removeDueDate = function() {
            BulkOperations.removeDueDate(currentCard())
        };

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe($scope, '/event/card/' + ctrl.card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;
            if(type === 'UPDATE_DESCRIPTION') {
                loadDescription();
            } else if(type.indexOf('LABEL') > -1) {
                loadLabelValues();
            }
        });

        ctrl.userPermissions = {};
        function loadUserPermissions() {
            User.hasPermissions(COMPONENT_PERMISSIONS, ctrl.project.shortName).then(function(permissions) {
                ctrl.userPermissions = permissions;
            });
        }
        loadUserPermissions();
    };
})();
