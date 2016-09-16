(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLabels', {
        bindings: {
            card: '<',
            project: '<',
            labelValues: '<'
        },
        controller: CardLabelsController,
        templateUrl: 'app/components/card/labels/card-labels.html'
    });

    function CardLabelsController(Label, BulkOperations, Notification) {

        var ctrl = this;
        var project = ctrl.project;
        var card = ctrl.card;

        ctrl.hasUserLabels = function() {
            if(project.metadata.userLabels === undefined || ctrl.labelValues === undefined) {
                return false;
            }

            for(var v in project.metadata.userLabels) {
            	if(ctrl.labelValues[project.metadata.userLabels[v].id] !== undefined) {
            		return true;
            	}
            }

            return false;
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
