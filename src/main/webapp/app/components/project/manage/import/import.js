(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageImport', {
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/manage/import/import.html',
        controller: ['$timeout', 'StompClient', 'Project', 'Admin', 'Notification', 'Board', ProjectManageImportController],
    });

    function ProjectManageImportController($timeout, StompClient, Project, Admin, Notification, Board) {
        var ctrl = this;
        //

        ctrl.connectToTrello = connectToTrello;
        ctrl.checkShortName = checkShortName;
        ctrl.suggestShortName = suggestShortName;
        ctrl.manageImports = manageImports;
        ctrl.cancel = cancel;
        ctrl.importFromTrello = importFromTrello;
        ctrl.loadTrello = loadTrello;
        ctrl.isValidShortNames = isValidShortNames;
        //

        var stompSub = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.importSettings = {id: generateUUID(), archived: false};
            ctrl.availableOrganizations = [];
            ctrl.boardsToImport = 0;
            ctrl.view = {};

            stompSub = StompClient.subscribe('/event/import/' + ctrl.importSettings.id, function (message) {
                var body = JSON.parse(message.body);

                ctrl.view.progress = 100.0 * body['currentBoard'] / body['boards'];
                ctrl.view.currentBoardName = body['boardName'];
            });

            Admin.findByKey('TRELLO_API_KEY').then(function (key) {
                if (!angular.isDefined(key.second) || key.second === null) {
                    ctrl.hasApiKey = false;

                    return;
                }

                ctrl.hasApiKey = true;
                ctrl.trelloApiKey = key.second;
                ctrl.trelloSecret = '';
                ctrl.trelloClientUrl = 'https://api.trello.com/1/client.js?key=' + ctrl.trelloApiKey;
            });
        };

        ctrl.$onDestroy = function onDestroy() {
            stompSub();
        };
        //

        function checkShortName(board) {
            if (!angular.isDefined(board.shortName) || board.shortName === null || board.shortName === '') {
                board.checkedShortName = false;

                return;
            }
            Board.checkShortName(board.shortName).then(function (res) {
                board.checkedShortName = res;
            });
        }

        function suggestShortName(board) {
            if (board === null || !angular.isDefined(board.name) || board.name === null || board.name === '') {
                return;
            }
            Board.suggestShortName(board.name).then(function (res) {
                board.shortName = res.suggestion;
                ctrl.checkShortName(board);
            });
        }

        function isValidShortNames() {
            var isValid = true;

            angular.forEach(ctrl.availableOrganizations, function (o) {
                angular.forEach(o.boards, function (b) {
                    if (b.import) {
                        isValid = isValid && b.checkedShortName;
                    }
                });
            });

            return isValid;
        }

        function manageImports(board) {
            ctrl.boardsToImport += board.import ? 1 : -1;
            ctrl.suggestShortName(board);
        }

        function cancel() {
            ctrl.availableOrganizations = [];
            ctrl.view.progress = 0;
            ctrl.view.currentBoardName = undefined;
        }

        function importFromTrello() {
            ctrl.view.trelloImportIsRunning = true;
            ctrl.view.progress = 0;
            ctrl.view.currentBoardName = undefined;
            var boardsToImport = [];

            angular.forEach(ctrl.availableOrganizations, function (o) {
                angular.forEach(o.boards, function (b) {
                    if (b.import) {
                        boardsToImport.push({id: b.id, shortName: b.shortName});
                        b.import = false;
                        b.shortName = undefined;
                        b.checkedShortName = undefined;
                    }
                });
            });

            Project.importFromTrello({
                apiKey: ctrl.trelloApiKey,
                secret: ctrl.trelloSecret,
                projectShortName: ctrl.project.shortName,
                importId: ctrl.importSettings.id,
                importArchived: ctrl.importSettings.archived,
                boards: boardsToImport
            }).then(function () {
                ctrl.view.trelloImportIsRunning = false;
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-import.importFromTrello.success'}, false);
            });
        }

        function loadTrello() {
            if (!window.Trello) {
                requireScript('/js/jquery-3.1.0.slim.min.js', function () {
                    requireScript(ctrl.trelloClientUrl, function () {
                        $timeout(function () {
                            ctrl.trelloLoaded = true;
                        });
                    });
                });
            } else {
                ctrl.trelloLoaded = true;
            }
        }

        function connectToTrello() {
            Trello.authorize({
                type: 'popup',
                name: 'Lavagna',
                expiration: '1day',
                persist: false,
                scope: {read: true, write: false, account: true},
                success: function () {
                    $timeout(function () {
                        ctrl.view.connectingToTrello = true;
                        ctrl.trelloSecret = Trello.token();
                        Project.getAvailableTrelloBoards({
                            apiKey: ctrl.trelloApiKey,
                            secret: ctrl.trelloSecret
                        }).then(function (result) {
                            ctrl.view.connectingToTrello = false;
                            ctrl.availableOrganizations = result.organizations;
                        });
                    });
                },
                error: function () {
                    $timeout(function () {
                        ctrl.view.connectingToTrello = false;
                        Notification.addAutoAckNotification('error', {key: 'notification.project-manage-import.importFromTrello.error'}, false);
                    });
                }
            });
        }
    }

    // http://stackoverflow.com/a/14174028
    function requireScript(url, callback) {
        var script = document.createElement('script');

        script.src = url;
        script.type = 'text/javascript';
        script.onload = callback;
        // Internet explorer
        script.onreadystatechange = function () {
            if (this.readyState === 'complete') {
                callback();
            }
        };
        document.getElementsByTagName('head')[0].appendChild(script);
    }

    function generateUUID() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = (d + Math.random() * 16) % 16 | 0;

            d = Math.floor(d / 16);

            return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });

        return uuid;
    }
}());
