(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectBoards', {
        bindings: {
            project: '<',
            user: '<'
        },
        templateUrl: 'app/components/project/boards/project-boards.html',
        controller: ['$mdDialog', 'Project', 'Board', 'User', 'Notification', 'StompClient', ProjectController],
    });

    function ProjectController($mdDialog, Project, Board, User, Notification, StompClient) {
        var ctrl = this;

        //
        ctrl.fetchUserCardsInProjectPage = fetchUserCardsInProjectPage;
        ctrl.showBoardDialog = showBoardDialog;
        ctrl.updateShowArchivedItems = updateShowArchivedItems;
        ctrl.reloadBoards = loadBoardsInProject;
        //

        var onDestroyStomp = angular.noop;
        var metadataSubscription = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.boardPage = 1;
            ctrl.boardsPerPage = 10;
            ctrl.maxVisibleBoardPages = 3;
            ctrl.cardProjectPage = 1;

            metadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl);

            loadBoardsInProject();
            loadUserCardsInProject(ctrl.cardProjectPage - 1);

            onDestroyStomp = StompClient.subscribe('/event/project/' + ctrl.project.shortName + '/board', loadBoardsInProject);

            ctrl.showArchivedItems = ctrl.user.userMetadata && ctrl.user.userMetadata.showArchivedBoards || false;
        };

        ctrl.$onDestroy = function onDestroy() {
            metadataSubscription();
            onDestroyStomp();
        };

        //

        function loadBoardsInProject() {
            User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Project.findBoardsInProject(ctrl.project.shortName);
            }).then(function (b) {
                ctrl.boards = b;
            });
        }

        function loadUserCardsInProject(page) {
            User.isAuthenticated().then(function () { return User.hasPermission('SEARCH'); }).then(function () {
                User.cardsByProject(ctrl.project.shortName, page).then(function (cards) {
                    ctrl.totalProjectOpenCards = cards.count;
                    ctrl.cardsCurrentPage = cards.currentPage + 1;
                    ctrl.cardsTotalPages = cards.totalPages;
                    ctrl.userProjectCards = cards.found.slice(0, cards.countPerPage);
                });
            });
        }

        function fetchUserCardsInProjectPage(page) {
            loadUserCardsInProject(page - 1);
        }

        function showBoardDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/project/boards/add-board-dialog.html',
                fullscreen: true,
                controllerAs: 'boardDialogCtrl',
                locals: {projectName: ctrl.project.shortName},
                bindToController: true,
                controller: function () {
                    var ctrl = this;

                    ctrl.board = {};

                    ctrl.errors = {};

                    ctrl.suggestBoardShortName = function (board) {
                        if (board === null || !angular.isDefined(board.name) || board.name === null || board.name === '') {
                            return;
                        }
                        Board.suggestShortName(board.name).then(function (res) {
                            board.shortName = res.suggestion;
                            ctrl.checkBoardShortName(res.suggestion);
                        });
                    };

                    ctrl.checkBoardShortName = function (val) {
                        if (val !== undefined && val !== null) {
                            Board.checkShortName(val).then(function (res) {
                                ctrl.errors.shortName = res === false ? true : undefined;
                            });
                        } else {
                            ctrl.errors.shortName = undefined;
                        }
                    };

                    ctrl.createBoard = function (board) {
                        board.shortName = board.shortName.toUpperCase();
                        Project.createBoard(ctrl.projectName, board).then(function () {
                            board.name = null;
                            board.description = null;
                            board.shortName = null;
                            ctrl.checkedShortName = undefined;
                            Notification.addAutoAckNotification('success', {key: 'notification.board.creation.success'}, false);
                        }, function () {
                            Notification.addAutoAckNotification('error', {key: 'notification.board.creation.error'}, false);
                        });
                    };

                    ctrl.close = function () {
                        $mdDialog.hide();
                    };
                }
            });
        }

        function updateShowArchivedItems(value) {
            var metadata = ctrl.user.userMetadata || {};

            metadata.showArchivedBoards = value;
            User.updateMetadata(metadata).then(User.current, User.current).then(function (user) {
                ctrl.user = user;
                ctrl.showArchivedItems = user.userMetadata.showArchivedBoards;
            });
        }
    }
}());

