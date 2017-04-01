(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    // simple wrapper
    services.factory('Tooltip', ['$mdPanel', '$q', Tooltip]);

    function Tooltip($mdPanel, $q) {
        function cleanUpRogueTooltips() {
            angular.forEach($mdPanel._trackedPanels, function (value, id) {
                delete $mdPanel._trackedPanels[id];
            });

            var tooltips = document.querySelectorAll('.lvg-tooltip__panel');

            angular.forEach(tooltips, function (tooltip) {
                tooltip.parentNode.parentNode.removeChild(tooltip.parentNode);
            });
        }

        return {
            clean: function () {
                return $q(function (resolve) {
                    angular.forEach($mdPanel._trackedPanels, function (value, id) {
                        if (id.indexOf('lvg-tooltip') === 0) {
                            value.close();
                        }
                    });
                    cleanUpRogueTooltips();
                    resolve();
                });
            },
            card: function (card, metadata, user, element) {
                var position = $mdPanel.newPanelPosition()
                    .relativeTo(element)
                    .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW)
                    .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.BELOW)
                    .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.ABOVE)
                    .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.ABOVE);
                var conf = {
                    id: 'lvg-tooltip-card-' + card.id,
                    controller: function (mdPanelRef, metadata) {
                        this.mdPanelRef = mdPanelRef;
                        this.metadata = metadata;
                    },
                    controllerAs: '$ctrl',
                    template: '<lvg-card-fragment-v2 view="board" hide-select="true" hide-menu="true" read-only="true" card-ref="$ctrl.card" user-ref="$ctrl.user" project-metadata-ref="$ctrl.metadata" class="lvg-card-fragment-v2__tooltip lvg-card-fragment-v2__static"></lvg-card-fragment-v2>',
                    panelClass: 'lvg-tooltip__panel',
                    position: position,
                    focusOnOpen: false,
                    propagateContainerEvents: true,
                    groupName: 'lvg-tooltip',
                    locals: {
                        card: card,
                        user: user
                    },
                    resolve: {
                        metadata: metadata
                    }
                };

                this.clean().then(function () {
                    $mdPanel.open(conf);
                });
            },
            user: function (user, element) {
                var position = $mdPanel.newPanelPosition()
                    .relativeTo(element)
                    .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW)
                    .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.BELOW)
                    .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.ABOVE)
                    .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.ABOVE);
                var conf = {
                    id: 'lvg-tooltip-user-' + user.id,
                    controller: function (mdPanelRef) {
                        this.mdPanelRef = mdPanelRef;
                    },
                    controllerAs: '$ctrl',
                    template: '<lvg-user-tooltip user-ref="$ctrl.user" class="lvg-user__tooltip"></lvg-user-tooltip>',
                    panelClass: 'lvg-tooltip__panel',
                    position: position,
                    focusOnOpen: false,
                    propagateContainerEvents: true,
                    groupName: 'lvg-tooltip',
                    locals: {
                        user: user
                    }
                };

                this.clean().then(function () {
                    $mdPanel.open(conf);
                });
            }
        };
    }
}());
