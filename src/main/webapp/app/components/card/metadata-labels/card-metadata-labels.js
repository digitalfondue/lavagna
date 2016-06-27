(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardMetadataLabels', {
        bindings: {
            card: '<',
            project: '<',
            labelValues: '<'
        },
        controller: CardMetadataLabelsController,
        templateUrl: 'app/components/card/metadata-labels/card-metadata-labels.html'
    });

    function CardMetadataLabelsController(Label, BulkOperations, Notification) {

        var ctrl = this;
        var project = ctrl.project;
        var card = ctrl.card;
        ctrl.labels = ctrl.project.metadata.userLabels;

        ctrl.hasUserLabels = function(userLabels, labelValues) {
            if(userLabels === undefined || labelValues === undefined) {
                return false;
            }
            var count = 0;
            for(var n in userLabels) {
                count += labelValues[n] === undefined ? 0 : labelValues[n].length;
            }
            return count > 0;
        };

        var currentCard = function() {
            var cardByProject = {};
            cardByProject[project.shortName] = [card.id];
            return cardByProject;
        };

        ctrl.removeLabelValue = function(label, labelValue) {

            BulkOperations.removeLabel(currentCard(), {id: labelValue.labelId}, labelValue.value).then(function(data) {

                    Notification.addAutoAckNotification('success', {
                        key : 'notification.card.LABEL_DELETE.success',
                        parameters : {
                            labelName : label.name }
                    }, false);

            }, function(error) {
                ctrl.actionListState[listId].deleteList = false;

                    Notification.addAutoAckNotification('error', {
                        key : 'notification.card.LABEL_DELETE.error',
                        parameters : {
                            labelName : label.name }
                    }, false);

            })
        };

    };
})();
