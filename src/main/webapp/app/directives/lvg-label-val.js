(function () {

    'use strict';

    var components = angular.module('lavagna.directives');

    components.component('lvgLabelVal', {
        bindings: {
            value: '=',
            project:'<'
        },
        controller: ['$filter', '$element', '$rootScope', '$state', '$window', 'CardCache', 'UserCache', 'ProjectCache', lvgLabelValV2Ctrl]
    });

    function lvgLabelValV2Ctrl($filter, $element, $rootScope, $state, $window, CardCache, UserCache, ProjectCache) {
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
                handleList(ctrl.project, ctrl_value.labelId, value.valueList);
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

                element.attr('href', $state.href('user', {provider: user.provider, username: user.username}));

                element.text($filter('formatUser')(user));
                if (!user.enabled) {
                    element.addClass('user-disabled');
                }

            });
        }

        //-------------

        function handleList(projectShortName, labelId, valueList) {

            var a = $window.document.createElement('a');
            $element.append(a);

            var updateFromCache = function (valueList) {
                ProjectCache.getMetadata(projectShortName).then(function (metadata) {
                    if (metadata && metadata.labelListValues && metadata.labelListValues[valueList]) {

                        if (metadata.labels[labelId].domain === 'SYSTEM' && metadata.labels[labelId].name === 'MILESTONE') {
                            var element = angular.element(a);
                            element.attr('href', $state.href('projectMilestone', {
                                projectName: projectShortName,
                                milestone: metadata.labelListValues[valueList].value
                            }));
                            element.text(metadata.labelListValues[valueList].value);
                            // Refresh milestone CLOSED status
                            if (metadata.labelListValues[valueList].metadata &&
                                metadata.labelListValues[valueList].metadata.status === 'CLOSED') {
                                element.addClass('strike');
                            } else {
                                element.removeClass('strike');
                            }
                        } else {
                            appendValueToElement(metadata.labelListValues[valueList].value);
                        }

                    }
                });
            };

            var toDismiss = $rootScope.$on('refreshProjectMetadataCache-' + projectShortName, function () {
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
            var span = $window.document.createElement('span');
            a.appendChild(span);

            CardCache.card(cardId).then(function (card) {
                var aElement = angular.element(a);
                var spanElement = angular.element(span);

                span.textContent = card.boardShortName + '-' + card.sequence;
                aElement.attr('href', $state.href('projectBoard.card', {
                    projectName: card.projectShortName,
                    shortName: card.boardShortName,
                    seqNr: card.sequence
                }));

                updateCardClass(card, spanElement);

                var toDismiss = $rootScope.$on('refreshCardCache-' + cardId, function () {
                    CardCache.card(cardId).then(function (card) {
                        updateCardClass(card, spanElement);
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
