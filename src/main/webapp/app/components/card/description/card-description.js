(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardDescription', {
        bindings: {
            project: '<',
            card: '<',
            labelValues: '<'
        },
        templateUrl: 'app/components/card/description/card-description.html',
        controller: ['BulkOperations', 'Card', 'Label', 'StompClient', CardDescriptionController],
    });

    function CardDescriptionController(BulkOperations, Card, Label, StompClient) {
        var ctrl = this;

        ctrl.addLabel = addLabel;
        ctrl.removeLabelValue = removeLabelValue;
        ctrl.hideAddPanel = hideAddPanel;
        ctrl.updateCardName = updateCardName;
        ctrl.updateDescription = updateDescription;
        ctrl.hasUserLabels = hasUserLabels;

        //

        var onDestroyStomp = angular.noop;

        ctrl.$onInit = function init() {
            loadDescription();

            // the /card-data has various card data related event that are pushed from the server that we must react
            onDestroyStomp = StompClient.subscribe('/event/card/' + ctrl.card.id + '/card-data', function (e) {
                var type = JSON.parse(e.body).type;

                if (type === 'UPDATE_DESCRIPTION') {
                    loadDescription();
                }
            });
        };

        ctrl.$onDestroy = function onDestroy() {
            onDestroyStomp();
        };

        // -----
        function updateCardName(newName) {
            Card.update(ctrl.card.id, newName);
        }

        function updateDescription(description) {
            Card.updateDescription(ctrl.card.id, description);
        }

        function addLabel($label) {
            var labelValueToUpdate = Label.extractValue($label.label, $label.value);

            BulkOperations.addLabel(currentCard(), $label.label, labelValueToUpdate);
        }

        function removeLabelValue($label, $labelValue) {
            BulkOperations.removeLabel(currentCard(), {id: $labelValue.labelId}, $labelValue.value).then(function () {
                Notification.addAutoAckNotification('success', {
                    key: 'notification.card.LABEL_DELETE.success',
                    parameters: {
                        labelName: $label.name }
                }, false);
            }, function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.card.LABEL_DELETE.error',
                    parameters: {
                        labelName: $label.name }
                }, false);
            });
        }

        function hideAddPanel() {
            ctrl.addLabelPanel = false;
        }

        function loadDescription() {
            Card.description(ctrl.card.id).then(function (description) {
                ctrl.description = description;
            });
        }

        function hasUserLabels() {
            if (ctrl.project.metadata.userLabels === undefined || ctrl.labelValues === undefined) {
                return false;
            }

            for (var v in ctrl.project.metadata.userLabels) {
                if (ctrl.labelValues[ctrl.project.metadata.userLabels[v].id] !== undefined) {
                    return true;
                }
            }

            return false;
        }

        function currentCard() {
            var cardByProject = {};

            cardByProject[ctrl.project.shortName] = [ctrl.card.id];

            return cardByProject;
        }
    }
}());
