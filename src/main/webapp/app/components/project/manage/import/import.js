(function() {
    'use strict';

    var components = angular.module('lavagna.components');
    components.component('lvgProjectManageImport', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageImportController,
        controllerAs: 'manageImportCtrl',
        templateUrl: 'app/components/project/manage/import/import.html'
    });

    function generateUUID(){
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = (d + Math.random()*16)%16 | 0;
            d = Math.floor(d/16);
            return (c=='x' ? r : (r&0x3|0x8)).toString(16);
        });
        return uuid;
    };

    function ProjectManageImportController($scope, StompClient, Project, Admin, Notification, Board) {

        var ctrl = this;
        ctrl.view = {};

        ctrl.importSettings = {id: generateUUID(), archived: false};
        ctrl.availableOrganizations = [];
        ctrl.boardsToImport = 0;

        StompClient.subscribe($scope, '/event/import/' + ctrl.importSettings.id, function (message) {
            var body = JSON.parse(message.body);
            ctrl.view.progress = 100.0 * body["currentBoard"] / body["boards"];
            ctrl.view.currentBoardName = body["boardName"];
        });

        ctrl.checkShortName = function (board) {
            if(board.shortName == null || board.shortName == "") {
                board.checkedShortName = false;
                return;
            }
            Board.checkShortName(board.shortName).then(function (res) {
                board.checkedShortName = res;
            });
        };

        ctrl.suggestShortName = function(board) {
            if(board == null ||board.name == null || board.name == "") {
                return;
            }
            Board.suggestShortName(board.name).then(function(res) {
                board.shortName = res.suggestion;
                ctrl.checkShortName(board);
            });
        };

        ctrl.manageImports = function(board) {
            ctrl.boardsToImport += board.import ? 1 : -1;
            ctrl.suggestShortName(board);
        }

        ctrl.cancel = function () {
            ctrl.availableOrganizations = [];
            ctrl.view.progress = 0;
            ctrl.view.currentBoardName = undefined;
        };


        Admin.findByKey('TRELLO_API_KEY').then(function (key) {
            if (key.second == null) {
                ctrl.hasApiKey = false;
                return;
            }

            ctrl.hasApiKey = true;

            var trelloApiKey = key.second;
            var trelloSecret = '';
            ctrl.trelloClientUrl = 'https://api.trello.com/1/client.js?key=' + trelloApiKey;

            ctrl.importFromTrello = function () {
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
                    apiKey: trelloApiKey,
                    secret: trelloSecret,
                    projectShortName: ctrl.project.shortName,
                    importId: ctrl.importSettings.id,
                    importArchived: ctrl.importSettings.archived,
                    boards: boardsToImport
                }).then(function () {
                    ctrl.view.trelloImportIsRunning = false;
                    Notification.addAutoAckNotification('success', {key: 'notification.project-manage-import.importFromTrello.success'}, false);
                });
            };
            
            //
            // 
            // http://stackoverflow.com/a/14174028
            function requireTrelloScript(callback){
                var head = document.getElementsByTagName("head")[0];
                var script = document.createElement('script');
                script.src = ctrl.trelloClientUrl;
                script.type = 'text/javascript';
                script.onload = callback;
                //Internet explorer
                script.onreadystatechange = function() {
                    if (this.readyState == 'complete') {
                        callback();
                    }
                }
                head.appendChild(script);
            }
            
            //
            
            ctrl.loadTrello = function() {
            	if(!window.Trello) {
	            	requireTrelloScript(function() {
	            		$scope.$applyAsync(function() {
	            			ctrl.trelloLoaded = true;
	            		})
	            	});
            	} else {
            		ctrl.trelloLoaded = true;
            	}
            }
            
            ctrl.connectToTrello = function() {
            	authorizeToTrello();
            }

            function authorizeToTrello() {
            	//
                Trello.authorize({
                    type: "popup",
                    name: "Lavagna",
                    expiration: "1day",
                    persist: false,
                    scope: {read: true, write: false, account: true},
                    success: function () {
                    	ctrl.view.connectingToTrello = true;
                        trelloSecret = Trello.token();
                        Project.getAvailableTrelloBoards({
                            apiKey: trelloApiKey,
                            secret: trelloSecret
                        }).then(function (result) {
                            ctrl.view.connectingToTrello = false;
                            ctrl.availableOrganizations = result.organizations;
                        });
                    },
                    error: function () {
                    	$scope.$applyAsync(function() {
                    		ctrl.view.connectingToTrello = false;
                    		Notification.addAutoAckNotification('error', {key: 'notification.project-manage-import.importFromTrello.error'}, false);
                    	});
                    }
                });
            }
        });

    };
})();
