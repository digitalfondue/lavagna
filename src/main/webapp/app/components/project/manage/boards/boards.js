(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgProjectManageBoards', {
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/manage/boards/boards.html',
        controller: ['Project', 'Board', 'Notification', ProjectManageBoardsController]
    });

    function ProjectManageBoardsController(Project, Board, Notification) {
        var ctrl = this;
        //

        ctrl.update = update;
        ctrl.archive = archive;
        ctrl.unarchive = unarchive;
        //

        ctrl.$onInit = function init() {
            ctrl.boards = {};
            reloadBoards();
        };

        function reloadBoards() {
            Project.findBoardsInProject(ctrl.project.shortName).then(function (boards) {
                ctrl.boards = boards;
            });
        }

        function update(boardToUpdate) {
            Board.update(boardToUpdate).then(reloadBoards).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.update.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.update.error'}, false);
            });
        }

        function archive(ab) {
            var boardToUpdate = {shortName: ab.shortName, name: ab.name, description: ab.description, archived: true};

            Board.update(boardToUpdate).then(reloadBoards).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.archive.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.archive.error'}, false);
            });
        }

        function unarchive(ab) {
            var boardToUpdate = {shortName: ab.shortName, name: ab.name, description: ab.description, archived: false};

            Board.update(boardToUpdate).then(reloadBoards).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.project-manage-boards.unarchive.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.project-manage-boards.unarchive.error'}, false);
            });
        }
    }
}());
