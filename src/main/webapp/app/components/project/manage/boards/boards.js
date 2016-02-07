(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageBoards', {
        bindings: {
            project: '='
        },
        controller: ProjectManageBoardsController,
        controllerAs: 'manageBoardsCtrl',
        templateUrl: 'app/components/project/manage/boards/boards.html'
    });

    function ProjectManageBoardsController($scope, Project, Board, Notification) {
        var ctrl = this;
        ctrl.view = {};
        ctrl.view.boardStatus = {};

        ctrl.boards = {};

        var reloadBoards = function () {
            Project.findBoardsInProject(ctrl.project.shortName).then(function (boards) {
                ctrl.boards = boards;
            });
        };

        reloadBoards();

        ctrl.update = function (boardToUpdate) {
            Board.update(boardToUpdate).then(function (b) {
                reloadBoards();
            }).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.update.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.update.error'}, false);
            });
        };

        ctrl.archive = function (ab) {
            var boardToUpdate = {shortName: ab.shortName, name: ab.name, description: ab.description, archived: true};
            Board.update(boardToUpdate).then(function (b) {
                reloadBoards();
            }).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.archive.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.archive.error'}, false);
            });
        };

        ctrl.unarchive = function (ab) {
            var boardToUpdate = {shortName: ab.shortName, name: ab.name, description: ab.description, archived: false};
            Board.update(boardToUpdate).then(function (b) {
                reloadBoards();
            }).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.unarchive.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.unarchive.error'}, false);
            });
        };
    }
})();
