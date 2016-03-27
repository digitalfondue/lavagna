(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgProjectBoards', {
        controller: ProjectController,
        controllerAs: 'projectCtrl',
        bindings: {
            project: '=',
            user: '='
        },
        templateUrl: 'app/components/project/boards/project-boards.html'
    });

    function ProjectController($scope, $mdDialog, Project, Board, User, Notification, StompClient) {
        var projectCtrl = this;

        var projectName = projectCtrl.project.shortName;
        
        
        Project.loadMetadataAndSubscribe(projectName, projectCtrl, $scope);
        
        //
        var r = [];
        projectCtrl.getMetadataHash = function() {
        	var hash = '';
        	if(projectCtrl.metadata) {
        		hash = projectCtrl.metadata.hash;
        	}
        	r[0] = hash;
        	return r;
        }
        
        //

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

        
        projectCtrl.showBoardDialog = function($event) {
		    $mdDialog.show({
		    	templateUrl: 'app/components/project/boards/add-board-dialog.html',
		    	targetEvent: $event,
		    	fullscreen:true,
		    	controllerAs: 'boardDialogCtrl',
		    	controller: function() {
		    		var ctrl = this;
		    		
		    		ctrl.board = {};
		            ctrl.isShortNameUsed = undefined;
		    		
		    		ctrl.suggestBoardShortName = function(board) {
		                if(board == null ||board.name == null || board.name == "") {
		                    return;
		                }
		                Board.suggestShortName(board.name).then(function(res) {
		                    board.shortName = res.suggestion;
		                    ctrl.checkBoardShortName(res.suggestion);
		                });
		            };
		            
		            ctrl.checkBoardShortName = function(val) {
		                if(val !== undefined && val !== null) {
		                	Board.checkShortName(val).then(function(res) {
		                		ctrl.checkedShortName = res;
		                    });
		                } else {
		                	ctrl.checkedShortName = undefined;
		                }
		            };
		            
		            ctrl.createBoard = function(board) {
		                board.shortName = board.shortName.toUpperCase();
		                Project.createBoard(projectName, board).then(function() {
		                    board.name = null;
		                    board.description = null;
		                    board.shortName = null;
		                    ctrl.checkedShortName = undefined;
		                    Notification.addAutoAckNotification('success', {key: 'notification.board.creation.success'}, false);
		                }, function(error) {
		                    Notification.addAutoAckNotification('error', {key: 'notification.board.creation.error'}, false);
		                });
		            };
		            
		            ctrl.close = function() {
                    	$mdDialog.hide();
                    }
		    	}
		    });
        }

        StompClient.subscribe($scope, '/event/project/' + projectName + '/board', loadBoardsInProject);
    }

})();

