(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('ProjectCtrl', function($stateParams, $scope, Project, Board, User, Notification, StompClient,
			project //resolved by ui-route
			) {

		var projectName = $stateParams.projectName;
		$scope.projectName = projectName;
		$scope.project = project;

		$scope.boardPage = 1;
		$scope.boardsPerPage = 10;
		$scope.maxVisibleBoardPages = 3;
		
		var loadBoardsInProject = function() {
			User.hasPermission('READ', projectName).then(function() {
				return Project.findBoardsInProject(projectName);
			}).then(function(b) {
				$scope.boards = b;
				$scope.totalBoards = b.length;
				$scope.switchBoardPage($scope.boardPage);
			});
		};

		$scope.switchBoardPage = function(page) {
			$scope.currentBoards = $scope.boards.slice((page - 1) * $scope.boardsPerPage,
					((page - 1) * $scope.boardsPerPage) + $scope.boardsPerPage);
		};

		loadBoardsInProject();

		$scope.cardProjectPage = 1;
		$scope.maxVisibleCardProjectPages = 3;

		var loadUserCardsInProject = function(page) {
			User.isAuthenticated().then(function() {return User.hasPermission('SEARCH')}).then(function() {
				User.cardsByProject(projectName, page).then(function(cards) {
					$scope.userProjectCards = cards.cards;
					$scope.totalProjectOpenCards = cards.totalCards;
					$scope.projectCardsPerPage = cards.itemsPerPage;
				});
			});
		};

		loadUserCardsInProject($scope.cardProjectPage - 1);

		$scope.fetchUserCardsInProjectPage = function(page) {
			loadUserCardsInProject(page - 1);
		};


		$scope.suggestBoardShortName = function(board) {
			if(board == null ||board.name == null || board.name == "") {
				return;
			}
			Board.suggestShortName(board.name).then(function(res) {
				board.shortName = res.suggestion;
			});
		};
		
		$scope.board = {};
		$scope.isShortNameUsed = undefined;
		
		$scope.$watch('board.shortName', function(newVal) {
			if(newVal !== undefined && newVal !== null) {
				Board.checkShortName(newVal).then(function(res) {
					$scope.checkedShortName = res;
				});
			} else {
				$scope.checkedShortName = undefined;
			}
		});

		StompClient.subscribe($scope, '/event/project/' + projectName + '/board', loadBoardsInProject);

		$scope.createBoard = function(board) {
			board.shortName = board.shortName.toUpperCase();
			Project.createBoard(projectName, board).then(function() {
				board.name = null;
				board.description = null;
				board.shortName = null;
				$scope.checkedShortName = undefined;
				Notification.addAutoAckNotification('success', {key: 'notification.board.creation.success'}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', {key: 'notification.board.creation.error'}, false);
			});
		};

	});

})();