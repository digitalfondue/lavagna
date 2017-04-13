(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFragmentV2', {
        bindings: {
            readOnly: '@', /* true | false (default) */
            view: '@', /* list | board | search */
            hideSelect: '@', /* "true" | ... */
            hideMenu: '@', /* "true" | ... */
            searchType: '@', /* globalSearch | projectSearch (default) */
            cardRef: '&',
            userRef: '&',
            projectMetadataRef: '&',
            selectedRef: '&',
            requiredPermissionsRef: '&'
        },
        controller: ['$element', '$scope', '$compile', '$state', '$location', '$filter', 'User', 'Card', 'EventBus', 'UserCache', 'CardCache', 'ProjectCache', 'Tooltip', '$mdPanel', CardFragmentV2Controller]
    });

    //
    var BASE_URL = document.querySelector('base').href;

    if (BASE_URL[BASE_URL.length - 1] !== '/') {
        BASE_URL += '/';
    }
    //

    function CardFragmentV2Controller($element, $scope, $compile, $state, $location, $filter, User, Card, EventBus, UserCache, CardCache, ProjectCache, Tooltip, $mdPanel) {
        var ctrl = this;

        var container = $element[0];

        ctrl.card = ctrl.cardRef();
        //
        ctrl.boardShortName = ctrl.card.boardShortName;
        ctrl.projectShortName = ctrl.card.projectShortName;
        //
        ctrl.user = ctrl.userRef();
        ctrl.projectMetadata = ctrl.projectMetadataRef();
        //
        ctrl.selected = ctrl.selectedRef();

        ctrl.readOnly = angular.isDefined(ctrl.readOnly);
        ctrl.listView = ctrl.view === 'list';
        ctrl.boardView = ctrl.view === 'board';
        ctrl.searchView = ctrl.view === 'search';

        ctrl.shortCardName = ctrl.card.boardShortName + '-' + ctrl.card.sequence;

        if (ctrl.hideSelect === undefined) {
            ctrl.hideSelect = false;
        }

        if (ctrl.hideMenu === undefined) {
            ctrl.hideMenu = false;
        }

        ctrl.requiredPermissions = ctrl.requiredPermissionsRef();

        //
        ctrl.isSelfWatching = ctrl.user && Card.isWatchedByUser(ctrl.card.labels, ctrl.user.id);
        ctrl.isAssignedToCard = ctrl.user && Card.isAssignedToUser(ctrl.card.labels, ctrl.user.id);
        //

        var headCtrl = new LvgCardFragmentV2HeadCtrl(container, $scope, $compile, $state, $location, $filter, $mdPanel, User, EventBus);

        headCtrl.lvgCardFragmentV2 = ctrl;
        headCtrl.$postLink();

        var dataInfoCtrl = new LvgCardFragmentV2DataInfoCtrl($filter, container, $mdPanel, $state, EventBus, UserCache, CardCache, ProjectCache, Tooltip);

        dataInfoCtrl.lvgCardFragmentV2 = ctrl;
        dataInfoCtrl.$postLink();

        if (!ctrl.listView && ctrl.isSelfWatching) {
            container.className += ' lvg-card-fragment-v2--watching';
        }

        if (ctrl.boardView && !User.checkPermissionInstant(ctrl.user, 'MOVE_CARD', ctrl.projectShortName)) {
            container.className += ' lvg-card-fragment-v2--not-draggable';
        }

        ctrl.$onDestroy = function () {
            headCtrl.$onDestroy();
            dataInfoCtrl.$onDestroy();
        };
    }

    function createElem(name) {
        return window.document.createElement(name);
    }

    function createText(value) {
        return window.document.createTextNode(value);
    }

    // --------------------
    function LvgCardFragmentV2HeadCtrl(domElement, $scope, $compile, $state, $location, $filter, $mdPanel, User, EventBus) {
        var ctrl = this;

        var subscribers = [];
        var BOARD_VIEW_CHECKBOX_PERMISSIONS = ['UPDATE_CARD', 'MOVE_CARD', 'MANAGE_LABEL_VALUE'];
        var BOARD_VIEW_MENU_PERMISSIONS = ['UPDATE_CARD', 'MOVE_CARD'];

        function hasAtLeastOnePermission(user, permissions, project) {
            if (!angular.isArray(permissions)) {
                return false;
            }

            for (var i = 0; i < permissions.length; i++) {
                if (User.checkPermissionInstant(user, permissions[i], project)) {
                    return true;
                }
            }

            return false;
        }

        ctrl.$postLink = function lvgCardFragmentV2HeadCtrlPostLink() {
            var parent = ctrl.lvgCardFragmentV2;

            var baseDiv = createElem('div');

            baseDiv.className = 'lvg-card-fragment-v2__card-head';

            if (parent.boardView && !parent.readOnly) {
                if (parent.hideSelect !== 'true' && hasAtLeastOnePermission(parent.user, BOARD_VIEW_CHECKBOX_PERMISSIONS, parent.card.projectShortName)) {
                    checkbox(parent.boardView, parent.selected, parent.card, subscribers, EventBus, $scope, domElement);
                }

                var mainLink = createMainLink('board.card', parent.projectShortName, parent.boardShortName, parent.card.sequence, true, false, $state, $location, subscribers, EventBus);

                baseDiv.appendChild(mainLink);

                // card fragment menu
                if (parent.hideMenu !== 'true' && hasAtLeastOnePermission(parent.user, BOARD_VIEW_MENU_PERMISSIONS, parent.card.projectShortName)) {
                    var buttonForMenu = createElem('button');

                    buttonForMenu.className = 'lvg-card-fragment-v2__menu lvg-icon__menu-vertical';
                    baseDiv.appendChild(buttonForMenu);

                    var menuEventListener = prepareOpenCardMenu(domElement, $scope, $compile, $mdPanel, buttonForMenu, parent.card, parent.isSelfWatching, parent.isAssignedToCard, parent.user, parent.projectMetadata);

                    buttonForMenu.addEventListener('click', menuEventListener);
                    subscribers.push(function () {
                        buttonForMenu.removeEventListener('click', menuEventListener);
                    });
                }
            } else if (parent.boardView && parent.readOnly) {
                baseDiv.appendChild(createText(parent.shortCardName));
                angular.element(baseDiv).addClass('lvg-card-fragment-v2__card-link');

                if (parent.hideMenu !== 'true' && hasAtLeastOnePermission(parent.user, BOARD_VIEW_MENU_PERMISSIONS, parent.card.projectShortName)) {
                    var buttonForMenuReadOnly = createElem('button');

                    buttonForMenuReadOnly.className = 'lvg-card-fragment-v2__menu lvg-icon__menu-vertical';
                    baseDiv.appendChild(buttonForMenuReadOnly);
                }

                if (parent.hideSelect !== 'true' && hasAtLeastOnePermission(parent.user, BOARD_VIEW_CHECKBOX_PERMISSIONS, parent.card.projectShortName)) {
                    var c = createElem('div');

                    c.className = 'lvg-card-fragment-v2__checkbox-container';
                    var fakeCheckbox = createElem('div');

                    fakeCheckbox.className = 'lvg-card-fragment-v2__checkbox';
                    c.appendChild(fakeCheckbox);
                    domElement.appendChild(c);
                }
            } else if (parent.listView) {
                var mainLinkListView = createMainLink('board.card', parent.projectShortName, parent.boardShortName, parent.card.sequence, false, false, $state, $location, subscribers, EventBus);

                baseDiv.appendChild(mainLinkListView);
                baseDiv.appendChild(createLastUpdateTime(parent.card.lastUpdateTime, $filter));
            } else if (parent.searchView) {
                if (hasAtLeastOnePermission(parent.user, parent.requiredPermissions, parent.card.projectShortName)) {
                    checkbox(parent.boardView, parent.selected, parent.card, subscribers, EventBus, $scope, domElement);
                }
                var route = parent.searchType === 'globalSearch' ? 'globalSearch.card' : 'projectSearch.card';
                var mainLinkSearchView = createMainLink(route, parent.projectShortName, parent.boardShortName, parent.card.sequence, true, parent.card.columnDefinition === 'CLOSED', $state, $location, subscribers, EventBus);

                baseDiv.appendChild(mainLinkSearchView);
                baseDiv.appendChild(createLastUpdateTime(parent.card.lastUpdateTime, $filter));
            }
            domElement.appendChild(baseDiv);
            domElement.appendChild(createText(parent.card.name));
        };

        ctrl.$onDestroy = function onDestroy() {
            for (var i = 0; i < subscribers.length; i++) {
                subscribers[i]();
            }
        };
    }

    function checkbox(isBoardView, selected, card, subscribers, EventBus, $scope, domElement) {
        var c = createElem('div');

        c.className = 'lvg-card-fragment-v2__checkbox-container';
        var fakeCheckbox = createElem('div');

        fakeCheckbox.className = 'lvg-card-fragment-v2__checkbox';
        var attrRole = document.createAttribute('role');

        attrRole.value = 'button';
        fakeCheckbox.attributes.setNamedItem(attrRole);
        var tabIndex = document.createAttribute('tabindex');

        tabIndex.value = '0';
        fakeCheckbox.attributes.setNamedItem(tabIndex);
        c.appendChild(fakeCheckbox);
        domElement.appendChild(c);

        var selectedState = isSelected();

        function isSelected() {
            if (isBoardView) {
                return (selected[card.columnId] && (selected[card.columnId][card.id])) === true;
            } else {
                return (selected[card.projectShortName] && (selected[card.projectShortName][card.id])) === true;
            }
        }

        function updateStyle() {
            if (selectedState) {
                domElement.classList.add('lvg-card-fragment-v2__selected');
            } else {
                domElement.classList.remove('lvg-card-fragment-v2__selected');
            }
        }

        function updateCheckbox() {
            selectedState = isSelected();
            updateStyle();
        }

        updateCheckbox();

        subscribers.push(EventBus.on('updatecheckbox', updateCheckbox));

        var handleClickEvent;

        if (isBoardView) {
            handleClickEvent = function handleClickEventBoardView() {
                $scope.$applyAsync(function () {
                    selectedState = !selectedState;
                    selected[card.columnId] = selected[card.columnId] || {};
                    selected[card.columnId][card.id] = selectedState;
                    updateStyle();
                });
            };
        } else {
            handleClickEvent = function handleClickEventBoardView() {
                $scope.$applyAsync(function () {
                    selectedState = !selectedState;
                    selected[card.projectShortName] = selected[card.projectShortName] || {};
                    selected[card.projectShortName][card.id] = selectedState;
                    updateStyle();
                });
            };
        }

        function handleKeyEvents(ev) {
            if (ev.keyCode === 13 || ev.keyCode === 32) {
                handleClickEvent();
                ev.preventDefault();
            }
        }

        fakeCheckbox.addEventListener('click', handleClickEvent);
        fakeCheckbox.addEventListener('keydown', handleKeyEvents);
        subscribers.push(function () {
            fakeCheckbox.removeEventListener('click', handleClickEvent);
            fakeCheckbox.removeEventListener('keydown', handleKeyEvents);
        });
    }

    function createLastUpdateTime(lastUpdateTime, $filter) {
        var e = angular.element(createElem('div')).addClass('lvg-card-fragment-v2__card-head-date')[0];

        e.textContent = $filter('dateIncremental')(lastUpdateTime);

        return e;
    }

    function createMainLink(targetState, projectName, boardShortName, sequenceNumber, isDynamicLink, cardIsClosed, $state, $location, subscribers, EventBus) {
        var a = createElem('a');

        a.className = 'lvg-card-fragment-v2__card-link';
        a.textContent = boardShortName + '-' + sequenceNumber;
        a.href = updateUrl($state, $location.search().q, $location.search().page, targetState, projectName, boardShortName, sequenceNumber);
        if (cardIsClosed) {
            a.className += ' lavagna-closed-card';
        }
        if (isDynamicLink) {
            var onUpdateQueryOrPageSub = EventBus.on('updatedQueryOrPage', function (ev, searchFilter) {
                a.href = updateUrl($state, searchFilter.location ? searchFilter.location.q : null, $location.search().page, targetState, projectName, boardShortName, sequenceNumber);
            });

            subscribers.push(onUpdateQueryOrPageSub);
        }

        return a;
    }

    function updateUrl($state, q, page, targetState, projectName, boardShortName, sequenceNumber) {
        if (targetState === 'board.card') {
            var cardUrl = BASE_URL + projectName + '/' + boardShortName + '-' + sequenceNumber;

            if (q !== undefined) {
                cardUrl += '?q=' + encodeURIComponent(q);
            }

            return cardUrl;
        }

        return $state.href(targetState, {
            projectName: projectName,
            shortName: boardShortName,
            seqNr: sequenceNumber,
            q: q,
            page: page
        });
    }

    function appendIfNotNull(parent, child) {
        if (child) {
            parent.appendChild(child);
        }
    }

    function updateCardClass(card, element) {
        if (card.columnDefinition !== 'CLOSED') {
            element.removeClass('lavagna-closed-card');
        } else {
            element.addClass('lavagna-closed-card');
        }
    }

    function addDueDateClasses(li, isTomorrow, isNow, isPast) {
        if (isTomorrow) {
            li.className += ' lvg-card-fragment-v2__card-data__due-date-tomorrow';
        }
        if (isNow) {
            li.className += ' lvg-card-fragment-v2__card-data__due-date-now';
        }
        if (isPast) {
            li.className += ' lvg-card-fragment-v2__card-data__due-date-past';
        }
    }

    function LvgCardFragmentV2DataInfoCtrl($filter, $element, $mdPanel, $state, EventBus, UserCache, CardCache, ProjectCache, Tooltip) {
        var ctrl = this;

        var card;
        var projectMetadata;
        var user;
        var notClosed;

        var listeners = [];

        var mouseOverCardElements = [];
        var mouseOverUserElements = [];

        ctrl.$postLink = function lvgCardFragmentV2DataInfoCtrlPostLink() {
            card = ctrl.lvgCardFragmentV2.card;
            projectMetadata = ctrl.lvgCardFragmentV2.projectMetadata;
            user = ctrl.lvgCardFragmentV2.user;
            notClosed = card.columnDefinition !== 'CLOSED';

            // labels
            handleLabels();

            // users
            handleAssigned();

            // metadata
            var liComment = handleComment();
            var liActionList = handleActionList();
            var liFiles = handleFiles();
            var liDueDate = handleDueDate();
            var liMilestone = handleMilestone();
            //

            if (liComment || liActionList || liFiles || liDueDate || liMilestone) {
                var divWrapper = createElem('div');

                $element.appendChild(divWrapper);
                appendIfNotNull(divWrapper, liComment);
                appendIfNotNull(divWrapper, liActionList);
                appendIfNotNull(divWrapper, liFiles);
                appendIfNotNull(divWrapper, liDueDate);
                appendIfNotNull(divWrapper, liMilestone);
            }
        };

        ctrl.$onDestroy = function lvgCardFragmentV2DataInfoCtrlOnDestroy() {
            Tooltip.clean();

            var i;
            var element;

            for (i = 0; i < listeners.length; i++) {
                listeners[i]();
            }
            for (i = 0; i < mouseOverCardElements.length; i++) {
                element = mouseOverCardElements[i];

                element.removeEventListener('mouseenter', handleMouseEnterCard);
                element.removeEventListener('mouseleave', handleMouseLeave);
            }
            for (i = 0; i < mouseOverUserElements.length; i++) {
                element = mouseOverUserElements[i];

                element.removeEventListener('mouseenter', handleMouseEnterUser);
                element.removeEventListener('mouseleave', handleMouseLeave);
            }
        };

        //

        function handleMouseEnterCard($event) {
            Tooltip.card($event.target.card, function () {
                return $event.target.card.projectShortName === projectMetadata.shortName ?
                    projectMetadata :
                    ProjectCache.metadata($event.target.card.projectShortName);
            }, user, $event.target);
        }

        function handleMouseEnterUser($event) {
            Tooltip.user($event.target.user, $event.target);
        }

        function handleMouseLeave() {
            Tooltip.clean();
        }

        //

        function hasCountGreaterThanZero(name) {
            return getCountOrZero(name) > 0;
        }

        function getCountOrZero(name) {
            return (card.counts && card.counts[name]) ? card.counts[name].count : 0;
        }

        function filterSystemLabelByName(labelName) {
            var res = [];

            for (var i = 0; i < card.labels.length; i++) {
                var label = card.labels[i];

                if (label.labelDomain === 'SYSTEM' && label.labelName === labelName) {
                    res.push(label);
                }
            }

            return res;
        }

        function filterUserLabels() {
            var res = [];

            for (var i = 0; i < card.labels.length; i++) {
                var label = card.labels[i];

                if (label.labelDomain === 'USER') {
                    res.push(label);
                }
            }

            return res;
        }

        function handleComment() {
            if (!(card.counts && card.counts['COMMENT'] && card.counts['COMMENT'].count > 0)) {
                return null;
            }

            var div = createElem('div');

            div.className = 'lvg-card-fragment-v2__card-data';

            appendIconAndText(div, 'comment', card.counts['COMMENT'].count);

            return div;
        }

        function handleActionList() {
            var hasActionListWithItems = hasCountGreaterThanZero('ACTION_CHECKED') || hasCountGreaterThanZero('ACTION_UNCHECKED');

            if (!hasActionListWithItems) {
                return null;
            }

            var checkedCount = getCountOrZero('ACTION_CHECKED');
            var uncheckedCount = getCountOrZero('ACTION_UNCHECKED');
            var actionItemsSummary = (checkedCount) + '/' + (checkedCount + uncheckedCount);

            var div = createElem('div');

            div.className = 'lvg-card-fragment-v2__card-data';

            if (card.columnDefinition === 'CLOSED' && uncheckedCount > 0) {
                div.className += ' lvg-card-fragment-v2__card-data__action-not-done';
            }

            appendIconAndText(div, 'list', actionItemsSummary);

            return div;
        }

        function handleFiles() {
            var hasFiles = hasCountGreaterThanZero('FILE');

            if (!hasFiles) {
                return null;
            }

            var filesCount = hasFiles ? getCountOrZero('FILE') : '';
            var div = createElem('div');

            div.className = 'lvg-card-fragment-v2__card-data';

            appendIconAndText(div, 'file', filesCount);

            return div;
        }

        function handleDueDate() {
            var dueDateLabels = filterSystemLabelByName('DUE_DATE');
            var hasDueDateLabel = dueDateLabels.length === 1;

            if (!hasDueDateLabel) {
                return null;
            }
            var dueDateLabel = dueDateLabels[0];
            // inline daysDiff filter
            var dueDate = moment(dueDateLabel.labelValueTimestamp);
            var daysDiff = moment().startOf('day').diff(dueDate, 'days');

            var isTomorrow = notClosed && daysDiff === -1;
            var isNow = notClosed && daysDiff === 0;
            var isPast = notClosed && daysDiff > 0;

            var div = createElem('div');

            div.className = 'lvg-card-fragment-v2__card-data';

            addDueDateClasses(div, isTomorrow, isNow, isPast);

            appendIconAndText(div, 'clock', $filter('date')(dueDate.toDate(), 'dd.MM.yyyy'));

            return div;
        }

        function handleMilestone() {
            var milestoneLabels = filterSystemLabelByName('MILESTONE');
            var hasMilestoneLabel = milestoneLabels.length === 1;

            if (!hasMilestoneLabel) {
                return null;
            }

            if (!projectMetadata || !projectMetadata.labelListValues) {
                return null;
            }

            var milestoneLabel = milestoneLabels[0];

            var releaseDateStr;
            var metadata = projectMetadata;

            var hasMilestoneMetadata = milestoneLabel && milestoneLabel.labelValueList && metadata.labelListValues[milestoneLabel.labelValueList];

            if (!hasMilestoneMetadata) {
                return null;
            }

            if (hasMilestoneMetadata && metadata.labelListValues[milestoneLabel.labelValueList].metadata) {
                releaseDateStr = metadata.labelListValues[milestoneLabel.labelValueList].metadata.releaseDate;
            }

            var div = createElem('div');

            div.className = 'lvg-card-fragment-v2__card-data lvg-card-fragment-v2__card-data__icon_milestone';

            if (releaseDateStr) {
                var releaseDate = moment(releaseDateStr, 'DD.MM.YYYY');// FIXME format server side
                var daysDiff = $filter('daysDiff')(releaseDate);

                var isTomorrow = (notClosed && daysDiff === -1);
                var isNow = (notClosed && daysDiff === 0);
                var isPast = (notClosed && daysDiff > 0);

                addDueDateClasses(div, isTomorrow, isNow, isPast);
            }

            var a = createElem('a');

            a.className = 'lvg-card-fragment-v2__card-link lvg-gray';
            if (projectMetadata.labelListValues[milestoneLabel.labelValueList].metadata.status === 'CLOSED') {
                a.className += ' strike';
            }
            a.textContent = projectMetadata.labelListValues[milestoneLabel.labelValueList].value;
            a.href = $state.href('project.milestones.milestone', {
                projectName: projectMetadata.shortName,
                id: projectMetadata.labelListValues[milestoneLabel.labelValueList].id
            });

            div.appendChild(a);

            return div;
        }

        // ------------
        function handleAssigned() {
            var assignedLabels = filterSystemLabelByName('ASSIGNED');

            if (assignedLabels.length === 0) {
                return;
            }

            var divWrapper = angular.element(createElem('div')).addClass('card-info')[0];
            var ul = angular.element(createElem('ul')).addClass('assigned-users')[0];

            divWrapper.appendChild(ul);
            for (var i = 0; i < assignedLabels.length; i++) {
                var userId = assignedLabels[i].value.valueUser;
                var a = handleUser(userId);
                var li = angular.element(createElem('li')).addClass('assigned-user')[0];

                li.appendChild(a);
                ul.appendChild(li);
            }
            $element.appendChild(divWrapper);
        }

        // ------------
        var labelBackgroundClass = $filter('labelBackgroundClass');
        var labelBackground = $filter('labelBackground');

        function handleLabels() {
            if (!projectMetadata || !projectMetadata.labels) {
                return null;
            }

            var userCreatedLabels = filterUserLabels();

            if (userCreatedLabels.length === 0) {
                return;
            }

            var divWrapper = createElem('div');

            divWrapper.className = 'lvg-card-fragment-v2__label-container';

            for (var i = 0; i < userCreatedLabels.length; i++) {
                var div = createElem('div');

                var value = userCreatedLabels[i];
                var bg = labelBackground(projectMetadata.labels[value.labelId].color);

                var textColorClass = 'lvg-card-fragment-v2__label-text-' + labelBackgroundClass(projectMetadata.labels[value.labelId].color);

                //
                var addSeparator = (value.labelValueType || value.type) !== 'NULL';
                var name = projectMetadata.labels[value.labelId].name;
                var nameAndSeparator = name + (addSeparator ? ': ' : '' );
                var userOrCardLink = null;

                switch (value.labelValueType) {
                    case 'NULL':
                        break;
                    case 'STRING':
                        nameAndSeparator += ' ' + value.value.valueString;
                        break;
                    case 'INT':
                        nameAndSeparator += ' ' + value.value.valueInt;
                        break;
                    case 'LIST':
                        nameAndSeparator += ' ' + projectMetadata.labelListValues[value.value.valueList].value;
                        break;
                    case 'TIMESTAMP':
                        nameAndSeparator += ' ' + $filter('date')(value.value.valueTimestamp, 'dd.MM.yyyy');
                        break;
                    case 'USER':
                        userOrCardLink = handleUser(value.value.valueUser, textColorClass, div);
                        break;
                    case 'CARD':
                        userOrCardLink = handleCard(value.value.valueCard, textColorClass, div);
                        break;
                }

                //
                div.className = 'lvg-card-fragment-v2__label ' + textColorClass;
                div.style.backgroundColor = bg['background-color'];
                div.textContent = nameAndSeparator;
                if (userOrCardLink) {
                    div.appendChild(userOrCardLink);
                }

                divWrapper.appendChild(div);
            }
            $element.appendChild(divWrapper);
        }

        // ------------

        function appendIconAndText(li, iconName, text) {
            li.className += (' lvg-card-fragment-v2__card-data__icon_' + iconName);
            li.appendChild(createText(' ' + text));
        }

        // -----------

        function handleUser(userId, textColorClass, labelDiv) {
            if (labelDiv) {
                labelDiv.style.display = 'none';
            }

            var a = createElem('a');

            a.className = textColorClass;
            UserCache.user(userId).then(function (user) {
                var element = angular.element(a);

                element.attr('href', $state.href('user.dashboard', {provider: user.provider, username: user.username}));
                element.text($filter('formatUser')(user));
                if (!user.enabled) {
                    element.addClass('user-disabled');
                }

                if (labelDiv) {
                    labelDiv.style.display = 'inline-block';
                }

                a.user = user;

                a.addEventListener('mouseenter', handleMouseEnterUser);
                a.addEventListener('mouseleave', handleMouseLeave);

                mouseOverUserElements.push(a);
            });

            return a;
        }

        function handleCard(cardId, textColorClass, labelDiv) {
            if (labelDiv) {
                labelDiv.style.display = 'none';
            }

            var a = createElem('a');

            CardCache.card(cardId).then(function (card) {
                var element = angular.element(a);

                a.textContent = card.boardShortName + '-' + card.sequence;
                a.className = textColorClass;
                a.card = card;
                element.attr('href', $state.href('board.card', {
                    projectName: card.projectShortName,
                    shortName: card.boardShortName,
                    seqNr: card.sequence
                }));
                a.addEventListener('mouseenter', handleMouseEnterCard);
                a.addEventListener('mouseleave', handleMouseLeave);

                updateCardClass(card, element);

                var toDismiss = EventBus.on('refreshCardCache-' + cardId, function () {
                    CardCache.card(cardId).then(function (card) {
                        updateCardClass(card, element);
                    });
                });

                listeners.push(toDismiss);

                mouseOverCardElements.push(a);

                if (labelDiv) {
                    labelDiv.style.display = 'inline-block';
                }
            });

            return a;
        }
    }

    function prepareOpenCardMenu(cardFragmentElement, $scope, $compile, $mdPanel, $element, card, isSelfWatching, isAssignedToCard, user, metadata) {
        return function (event) {
            var scopeForCardFragment = $scope.$new();

            scopeForCardFragment['card'] = card;
            scopeForCardFragment['user'] = user;
            scopeForCardFragment['metadata'] = metadata;
            var readOnlyCard = $compile('<lvg-card-fragment-v2 view="board" read-only="true" card-ref="card" user-ref="user" project-metadata-ref="metadata" class="lvg-card-fragment-v2__static"></lvg-card-fragment-v2>')(scopeForCardFragment)[0];

            copyPositionAndSize(cardFragmentElement, readOnlyCard);

            window.document.body.appendChild(readOnlyCard);
            var position = $mdPanel.newPanelPosition()
                .relativeTo($element)
                .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW)
                .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.BELOW)
                .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.ABOVE)
                .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.ABOVE);

            var panelCtrl = null;

            var conf = {
                attachTo: angular.element(document.body),
                controller: function (mdPanelRef) {
                    this.mdPanelRef = mdPanelRef;
                    readOnlyCard.style.display = 'block';
                    this.visibility = false;
                    panelCtrl = this;
                },
                controllerAs: '$ctrl',
                template: '<lvg-card-fragment-v2-menu visibility="$ctrl.visibility" md-panel-ref="$ctrl.mdPanelRef" card="$ctrl.card" current-user-id="$ctrl.currentUserId" is-self-watching="$ctrl.isSelfWatching" is-assigned-to-card="$ctrl.isAssignedToCard" class="lvg-card-fragment-v2-menu md-whiteframe-z2"></lvg-card-fragment-v2-menu>',
                position: position,
                openFrom: event,
                clickOutsideToClose: true,
                escapeToClose: true,
                trapFocus: true,
                focusOnOpen: true,
                onDomRemoved: function () {
                    readOnlyCard.remove();
                    scopeForCardFragment.$destroy();
                },
                locals: {
                    isSelfWatching: isSelfWatching,
                    isAssignedToCard: isAssignedToCard,
                    card: card,
                    currentUserId: user.id
                }
            };

            $mdPanel.open(conf).then(function () {
                panelCtrl.visibility = true;
            });
        };
    }

    function copyPositionAndSize(element, readOnlyCard) {
        var r = element.getBoundingClientRect();

        readOnlyCard.style.position = 'absolute';
        readOnlyCard.style.left = (r.left + window.scrollX) + 'px';
        readOnlyCard.style.top = (r.top + window.scrollY) + 'px';
        readOnlyCard.style.height = (r.height - 2 - 16) + 'px';
        readOnlyCard.style.width = (r.width - 2 - 16) + 'px';
        readOnlyCard.style.display = 'none';
        if (element.classList.contains('lvg-card-fragment-v2__selected')) {
            readOnlyCard.classList.add('lvg-card-fragment-v2__selected');
        }
    }
}());
