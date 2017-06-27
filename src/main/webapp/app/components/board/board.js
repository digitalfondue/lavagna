(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoard', {
        bindings: {
            project: '<',
            board: '<',
            userReference: '&'
        },
        templateUrl: 'app/components/board/board.html',
        controller: ['EventBus', '$location', '$filter', '$log', '$mdDialog', '$timeout',
            'Board', 'Card', 'Project', 'LabelCache', 'Search', 'StompClient', 'User', 'Notification', BoardController],
    });

    function BoardController(EventBus, $location, $filter, $log, $mdDialog, $timeout,
        Board, Card, Project, LabelCache, Search, StompClient, User, Notification) {
        var ctrl = this;
        //

        ctrl.moveCard = moveCard;
        ctrl.backFromLocation = backFromLocation;
        ctrl.dropColumn = dropColumn;
        ctrl.selectAll = selectAll;
        ctrl.unSelectAll = unSelectAll;
        ctrl.toggleSidebar = toggleSidebar;
        ctrl.formatBulkRequest = formatBulkRequest;
        ctrl.selectedVisibleCardsIdByColumnId = selectedVisibleCardsIdByColumnId;
        ctrl.selectedVisibleCount = selectedVisibleCount;
        ctrl.newCardAdvanced = newCardAdvanced;
        //

        var metadataSubscription = angular.noop;
        var refreshSearchSub = angular.noop;
        var stompSub = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.searchFilter = {};
            ctrl.user = ctrl.userReference();
            ctrl.currentUserId = ctrl.user.id;

            metadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl);

            // keep track of the selected cards
            ctrl.selectedCards = {};
            ctrl.columnsLocation = 'BOARD';

            stompSub = StompClient.subscribe('/event/board/' + ctrl.board.shortName + '/location/BOARD/column', function () {
                Board.columnsByLocation(ctrl.board.shortName, ctrl.columnsLocation).then(assignToColumn);
            });

            Board.columnsByLocation(ctrl.board.shortName, 'BOARD').then(assignToColumn);

            // -------------

            // will be used as a map columnState[columnId].editColumnName = true/false
            ctrl.columnState = {};

            ctrl.toggledSidebar = false;

            refreshSearchSub = EventBus.on('refreshSearch', function (ev, searchFilter) {
                try {
                    Search.buildSearchFilter(searchFilter.searchFilter, ctrl.columns, ctrl.user.id).then(function (filterFun) {
                        ctrl.searchFilter.cardFilter = filterFun;
                        ctrl.query = searchFilter.location.q;
                        $timeout(function () {
                            $location.search(searchFilter.location);
                            EventBus.emit('updatedQueryOrPage', searchFilter);
                        });
                    });
                } catch (e) {
                    $log.debug('parsing exception', e);
                }
            });
        };

        ctrl.$onDestroy = function onDestroy() {
            metadataSubscription();
            stompSub();
            refreshSearchSub();
        };

        function moveCard(card, location) {
            Card.moveAllFromColumnToLocation(card.columnId, [card.id], location);
        }

        function backFromLocation() {
            ctrl.locationOpened = false;
            ctrl.sidebarLocation = undefined;
        }

        function dropColumn(index, oldIndex) {
            var currentColIdx = oldIndex;
            var column = ctrl.columns[oldIndex];
            // same position, ignore drop

            if (currentColIdx === index) {
                return false;
            }

            ctrl.columns.splice(currentColIdx, 1);

            ctrl.columns.splice(index, 0, column);

            var colPos = [];

            angular.forEach(ctrl.columns, function (col) {
                colPos.push(col.id);
            });

            Board.reorderColumn(ctrl.board.shortName, ctrl.columnsLocation, colPos).catch(function () {
                Notification.addAutoAckNotification('error', { key: 'notification.generic.error'}, false);
            });
        }
        //

        function selectAll() {
            EventBus.emit('selectall');
        }

        function unSelectAll() {
            EventBus.emit('unselectall');
        }

        function selectedVisibleCardsId() {
            var ids = [];

            angular.forEach(selectedVisibleCardsIdByColumnId(), function (val) {
                ids = ids.concat(val);
            });

            return ids;
        }

        function selectedVisibleCardsIdByColumnId() {
            var res = {};

            angular.forEach(ctrl.selectedCards, function (column, columnId) {
                angular.forEach(column, function (isSelected, cardId) {
                    if (isSelected) {
                        if (!res[columnId]) {
                            res[columnId] = [];
                        }
                        res[columnId].push(parseInt(cardId, 10));
                    }
                });
            });

            return res;
        }

        function selectedVisibleCount() {
            return selectedVisibleCardsId().length;
        }
        //

        // -----------------------------------------------------------------------------------------------------

        function assignToColumn(columns) {
            ctrl.columns = columns;
            EventBus.emit('requestSearch');
        }

        // -----------------------------------------------------------------------------------------------------

        function formatBulkRequest() {
            var r = {};

            r[ctrl.project.shortName] = selectedVisibleCardsId();

            return r;
        }

        // -----------------------------------------------------------------------------------------------------

        // some sidebar controls

        function toggleSidebar($location) {
            if ($location === ctrl.sidebarLocation) {
                ctrl.toggledSidebar = !ctrl.toggledSidebar;
            } else {
                ctrl.sidebarLocation = $location;
                ctrl.toggledSidebar = true;
            }
        }

        function newCardAdvanced(column) {
            $mdDialog.show({
                autoWrap: false,
                template: '<md-dialog class="lvg-card-modal__dialog"><lvg-create-card project-short-name="vm.projectShortName" board-short-name="vm.boardShortName" column="vm.column" project-metadata="vm.metadata" user="vm.user"></lvg-create-card></md-dialog>',
                locals: {
                    projectShortName: ctrl.project.shortName,
                    boardShortName: ctrl.board.shortName,
                    user: ctrl.user,
                    metadata: ctrl.metadata,
                    column: column
                },
                bindToController: true,
                controller: function () {},
                controllerAs: 'vm',
                fullscreen: true
            });
        }
    }
}());
