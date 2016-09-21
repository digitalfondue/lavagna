(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgCardLabels', {
        bindings: {
            card: '<',
            project: '<',
            labelValues: '<',
        },
        controller: CardLabelsController,
        templateUrl: 'app/components/card/labels/card-labels.html'
    });

    function CardLabelsController(Label, BulkOperations, Notification) {

        var ctrl = this;
        
        ctrl.hasUserLabels = hasUserLabels;
        ctrl.removeLabelValue = removeLabelValue;
        
        
        ctrl.$onInit = function init() {
        	ctrl.labelValuesCntUpdate = 0;
        }
        
        ctrl.$onChanges = function onChanges(changes) {
        	if(changes.labelValues) {
        		ctrl.labelValuesCntUpdate++;
        	}
        }

        function hasUserLabels() {
            if(ctrl.project.metadata.userLabels === undefined || ctrl.labelValues === undefined) {
                return false;
            }

            for(var v in ctrl.project.metadata.userLabels) {
            	if(ctrl.labelValues[ctrl.project.metadata.userLabels[v].id] !== undefined) {
            		return true;
            	}
            }

            return false;
        };

        function currentCard() {
            var cardByProject = {};
            cardByProject[ctrl.project.shortName] = [ctrl.card.id];
            return cardByProject;
        };

        function removeLabelValue(label, labelValue) {

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
