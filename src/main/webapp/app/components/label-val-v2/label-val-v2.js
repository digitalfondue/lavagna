(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgLabelValV2', {
		bindings: {
			valueRef: '&',
			projectMetadataRef:'&'
		},
		controller: ['$filter', '$element', '$mdPanel', 'EventBus', '$state', '$window', 'CardCache', 'UserCache', LabelValV2Controller]
	});

	function LabelValV2Controller($filter, $element, $mdPanel, EventBus, $state, $window, CardCache, UserCache) {
		var ctrl = this;

		var elementDom = $element[0];

		var listeners = [];
        var mouseOverElements = [];
        var mouseOverPanelRef;

		ctrl.$postLink = function postLink() {

			var ctrl_value = ctrl.valueRef();
			var metadata = ctrl.projectMetadataRef();
			var type = ctrl_value.labelValueType || ctrl_value.type || ctrl_value.labelType;
			var value = ctrl_value.value || ctrl_value;

			if (type === 'STRING') {
				elementDom.textContent = value.valueString;
			} else if (type === 'INT') {
				elementDom.textContent = value.valueInt;
			} else if (type === 'USER') {
				handleUser(value.valueUser);
			} else if (type === 'CARD') {
			    elementDom.addEventListener('mouseleave', handleMouseLeave);
                elementDom.addEventListener('mousedown', handleMouseLeave);

				handleCard(value.valueCard, metadata);
			} else if (type === 'LIST' && metadata && metadata.labelListValues && metadata.labelListValues[value.valueList]) {
				elementDom.textContent = metadata.labelListValues[value.valueList].value;
			} else if (type === 'TIMESTAMP') {
				elementDom.textContent = $filter('date')(value.valueTimestamp, 'dd.MM.yyyy');
			}
		}

		ctrl.$onDestroy = function onDestroy() {
		    if(mouseOverPanelRef) {
                mouseOverPanelRef.close();
            }

		    for(var i = 0; i < listeners.length; i++) {
        				listeners[i]();
            }

            for(var i = 0; i < mouseOverElements.length; i++) {
                var element = mouseOverElements[i];
                element.removeEventListener('mouseenter', handleMouseEnter);
                element.removeEventListener('mouseleave', handleMouseLeave);
            }

            elementDom.removeEventListener('mouseleave', handleMouseLeave);
            elementDom.removeEventListener('mousedown', handleMouseLeave);
		};

		//-------------

		function handleMouseEnter($event) {

            if(mouseOverPanelRef) {
                mouseOverPanelRef.close();
            }

            var position = $mdPanel.newPanelPosition()
                .relativeTo($event.target)
                .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW)
                .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.BELOW)
                .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.ABOVE)
                .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.ABOVE)
            var conf = {
                    //attachTo: angular.element($event.target),
                    controller: function(mdPanelRef) {
                        this.mdPanelRef = mdPanelRef;
                        mouseOverPanelRef = mdPanelRef;
                    },
                    controllerAs: '$ctrl',
                    template: '<lvg-card-fragment-v2 view="board" hide-select="true" hide-menu="true" read-only="true" card-ref="$ctrl.card" user-ref="$ctrl.user" project-metadata-ref="$ctrl.metadata" class="lvg-card-fragment-v2__tooltip lvg-card-fragment-v2__static"></lvg-card-fragment-v2>',
                    panelClass: 'lvg-card-fragment-v2__tooltip-panel',
                    position: position,
                    focusOnOpen: false,
                    propagateContainerEvents: true,
                    locals: {
                        card: $event.target.card,
                        metadata: $event.target.metadata
                    }
            };
            $mdPanel.open(conf);
        };

		function handleMouseLeave($event) {
            if(mouseOverPanelRef) {
                mouseOverPanelRef.close();
            }
        };

		function handleUser(userId) {

			var a = $window.document.createElement('a');
			$element.append(a);

			UserCache.user(userId).then(function (user) {
				var element = angular.element(a);

				element.attr('href', $state.href('user.dashboard', {provider: user.provider, username: user.username}));

				element.text($filter('formatUser')(user));
				if (!user.enabled) {
					element.addClass('user-disabled');
				}

			});
		}
		//-------------

		function handleCard(cardId, metadata) {

			var a = $window.document.createElement('a');
			$element.append(a);

			CardCache.card(cardId).then(function (card) {
				var element = angular.element(a);

				a.textContent = card.boardShortName + '-' + card.sequence;
				a.card = card;
				a.metadata = metadata;
				element.attr('href', $state.href('board.card', {projectName: card.projectShortName, shortName: card.boardShortName, seqNr: card.sequence}));

				a.addEventListener('mouseenter', handleMouseEnter);
                a.addEventListener('mouseleave', handleMouseLeave);

				updateCardClass(card, element);

                mouseOverElements.push(a);

				var toDismiss = EventBus.on('refreshCardCache-' + cardId, function () {
					CardCache.card(cardId).then(function (card) {
						updateCardClass(card, element);
					});
				});

				listeners.push(toDismiss);

				ctrl.$onDestroy = function onDestroy() {
					toDismiss();
				};
			});
		}
	}

	function updateCardClass(card, element) {
		if (card.columnDefinition !== 'CLOSED') {
			element.removeClass('lavagna-closed-card');
		} else {
			element.addClass('lavagna-closed-card');
		}
	}

})();
