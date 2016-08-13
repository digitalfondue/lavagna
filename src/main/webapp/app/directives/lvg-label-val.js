(function () {

    'use strict';

    var components = angular.module('lavagna.directives');

    components.component('lvgLabelVal', {
        bindings: {
            value: '='
        },
        controller: ['$filter', '$element', '$rootScope', '$state', '$stateParams', '$window', 'CardCache', 'UserCache', 'ProjectCache', lvgLabelValV2Ctrl]
    });

    function lvgLabelValV2Ctrl($filter, $element, $rootScope, $state, $stateParams, $window, CardCache, UserCache, ProjectCache) {
        var ctrl = this;
        var ctrl_value = ctrl.value;

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
            } else if (type === 'LIST') {
                handleList(value.valueList);
            } else if (type === 'TIMESTAMP') {
                appendValueToElement($filter('date')(value.valueTimestamp, 'dd.MM.yyyy'));
            }
        };

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

        function handleList(valueList) {

            var updateFromCache = function (valueList) {
                ProjectCache.getMetadata($stateParams.projectName).then(function (metadata) {
                    if (metadata && metadata.labelListValues && metadata.labelListValues[valueList]) {
                        appendValueToElement(metadata.labelListValues[valueList].value);
                    }
                });
            };

            var toDismiss = $rootScope.$on('refreshProjectMetadataCache-' + $stateParams.projectName, function () {
                updateFromCache(valueList);
            });

            ctrl.$onDestroy = function onDestroy() {
                toDismiss();
            };

            updateFromCache(valueList);
        }

        function handleCard(cardId) {

            var a = $window.document.createElement('a');
            $element.append(a);

            CardCache.card(cardId).then(function (card) {
                var element = angular.element(a);

                a.textContent = card.boardShortName + '-' + card.sequence;
                element.attr('href', $state.href('board.card', {
                    projectName: card.projectShortName,
                    shortName: card.boardShortName,
                    seqNr: card.sequence
                }));

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
