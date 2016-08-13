(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgLabelValV2', {
		bindings: {
			valueRef: '&',
			projectMetadataRef:'&'
		},
		controller: ['$filter', '$element', '$rootScope', '$state', '$window', 'CardCache', 'UserCache', lvgLabelValV2Ctrl]
	});

	function lvgLabelValV2Ctrl($filter, $element, $rootScope, $state, $window, CardCache, UserCache) {
		var ctrl = this;
		var ctrl_value = ctrl.valueRef();

		var metadata = ctrl.projectMetadataRef();

		var type = ctrl_value.labelValueType || ctrl_value.type || ctrl_value.labelType;
		var value = ctrl_value.value || ctrl_value;

		ctrl.$postLink = function lvgLabelValV2PostLink() {
			if (type === 'STRING') {
				appendValueToElement(value.valueString);
			} else if (type === 'INT') {
				appendValueToElement(value.valueInt);
			} else if (type === 'USER') {
				handleUser(value.valueUser);
			} else if (type === 'CARD') {
				handleCard(value.valueCard);
			} else if (type === 'LIST' && metadata && metadata.labelListValues && metadata.labelListValues[value.valueList]) {
				appendValueToElement(metadata.labelListValues[value.valueList].value);
			} else if (type === 'TIMESTAMP') {
				appendValueToElement($filter('date')(value.valueTimestamp, 'dd.MM.yyyy'));
			}
		}

		//-------------

		function appendValueToElement(value) {
			$element[0].textContent = value;
		}

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

		function handleCard(cardId) {

			var a = $window.document.createElement('a');
			$element.append(a);

			CardCache.card(cardId).then(function (card) {
				var element = angular.element(a);

				a.textContent = card.boardShortName + '-' + card.sequence;
				element.attr('href', $state.href('board.card', {projectName: card.projectShortName, shortName: card.boardShortName, seqNr: card.sequence}));

				updateCardClass(card, element);

				var toDismiss = $rootScope.$on('refreshCardCache-' + cardId, function () {
					CardCache.card(cardId).then(function (card) {
						updateCardClass(card, element);
					});
				});

				ctrl.$onDestroy = function onDestroy() {
					toDismiss();
				};
			});
		}

		function updateCardClass(card, element) {
			if (card.columnDefinition != 'CLOSED') {
				element.removeClass('lavagna-closed-card');
			} else {
				element.addClass('lavagna-closed-card');
			}
		}
	}


})();
