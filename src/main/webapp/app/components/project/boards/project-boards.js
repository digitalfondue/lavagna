(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.directive('lvgComponentProject', ProjectComponent);

	function ProjectComponent(Project, Board, User, Notification, StompClient) {
	    return {
            restrict: 'E',
            scope: true,
            controller: ProjectController,
            controllerAs: 'projectCtrl',
            bindToController: {
                project: '='
            },
            templateUrl: 'app/components/project/boards/project-boards.html'
        };
    }

    function ProjectController($scope, Project, Board, User, Notification, StompClient) {
        var projectCtrl = this;

        var projectName = projectCtrl.project.shortName;

        projectCtrl.boardPage = 1;
        projectCtrl.boardsPerPage = 10;
        projectCtrl.maxVisibleBoardPages = 3;

        var loadBoardsInProject = function() {
            User.hasPermission('READ', projectName).then(function() {
                return Project.findBoardsInProject(projectName);
            }).then(function(b) {
                projectCtrl.boards = b;
                projectCtrl.totalBoards = b.length;
                projectCtrl.switchBoardPage(projectCtrl.boardPage);
            });
        };

        projectCtrl.switchBoardPage = function(page) {
            projectCtrl.currentBoards = projectCtrl.boards.slice((page - 1) * projectCtrl.boardsPerPage,
                    ((page - 1) * projectCtrl.boardsPerPage) + projectCtrl.boardsPerPage);
        };

        loadBoardsInProject();

        projectCtrl.cardProjectPage = 1;
        projectCtrl.maxVisibleCardProjectPages = 3;

        var loadUserCardsInProject = function(page) {
            User.isAuthenticated().then(function() {return User.hasPermission('SEARCH')}).then(function() {
                User.cardsByProject(projectName, page).then(function(cards) {
                    projectCtrl.userProjectCards = cards.cards;
                    projectCtrl.totalProjectOpenCards = cards.totalCards;
                    projectCtrl.projectCardsPerPage = cards.itemsPerPage;
                });
            });
        };

        loadUserCardsInProject(projectCtrl.cardProjectPage - 1);

        projectCtrl.fetchUserCardsInProjectPage = function(page) {
            loadUserCardsInProject(page - 1);
        };


        projectCtrl.suggestBoardShortName = function(board) {
            if(board == null ||board.name == null || board.name == "") {
                return;
            }
            Board.suggestShortName(board.name).then(function(res) {
                board.shortName = res.suggestion;
            });
        };

        projectCtrl.board = {};
        projectCtrl.isShortNameUsed = undefined;

        //TODO: remove this crap, use ng-change
        $scope.$watch('board.shortName', function(newVal) {
            if(newVal !== undefined && newVal !== null) {
                Board.checkShortName(newVal).then(function(res) {
                    projectCtrl.checkedShortName = res;
                });
            } else {
                projectCtrl.checkedShortName = undefined;
            }
        });

        StompClient.subscribe($scope, '/event/project/' + projectName + '/board', loadBoardsInProject);

        projectCtrl.createBoard = function(board) {
            board.shortName = board.shortName.toUpperCase();
            Project.createBoard(projectName, board).then(function() {
                board.name = null;
                board.description = null;
                board.shortName = null;
                projectCtrl.checkedShortName = undefined;
                Notification.addAutoAckNotification('success', {key: 'notification.board.creation.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.board.creation.error'}, false);
            });
        };
    }

})();

