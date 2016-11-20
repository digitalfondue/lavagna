(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	//simple wrapper
	services.factory('Tooltip', ['$mdPanel', Tooltip]);


	function Tooltip($mdPanel) {
		return {
			clean: function(ignore) {
				angular.forEach($mdPanel._trackedPanels, function(value, id) {
                    if(id !== ignore && id.indexOf('lvg-tooltip') === 0) {
                        value.close();
                    }
                });
			},
			close: function(tooltipId) {
                angular.forEach($mdPanel._trackedPanels, function(value, id) {
                    if(id === tooltipId) {
                        value.close();
                    }
                });
			},
			card: function(card, metadata, user, element) {
			    var position = $mdPanel.newPanelPosition()
                    .relativeTo(element)
                    .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW)
                    .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.BELOW)
                    .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.ABOVE)
                    .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.ABOVE)
                var conf = {
                    id: 'lvg-tooltip-card-' + card.id,
                    controller: function(mdPanelRef, metadata) {
                        this.mdPanelRef = mdPanelRef;
                        this.metadata = metadata;
                    },
                    controllerAs: '$ctrl',
                    template: '<lvg-card-fragment-v2 view="board" hide-select="true" hide-menu="true" read-only="true" card-ref="$ctrl.card" user-ref="$ctrl.user" project-metadata-ref="$ctrl.metadata" class="lvg-card-fragment-v2__tooltip lvg-card-fragment-v2__static"></lvg-card-fragment-v2>',
                    panelClass: 'lvg-card-fragment-v2__tooltip-panel',
                    position: position,
                    focusOnOpen: false,
                    propagateContainerEvents: true,
                    locals: {
                        card: card,
                        user: user
                    },
                    resolve: {
                        metadata: metadata
                    }
                };
                $mdPanel.open(conf);
			},
			user: function(user, element) {

			}
		}
	}

})();
