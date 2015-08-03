(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');
	
	/* http://stackoverflow.com/a/8809472 */
	function generateUUID(){
	    var d = new Date().getTime();
	    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	        var r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
	    });
	    return uuid;
	};

	module.controller('ManageImportCtrl', function ($scope, StompClient, Project, Admin, project, Notification, Board) {

		$scope.project = project;
		$scope.importSettings = {id: generateUUID(), archived: false};
		$scope.availableOrganizations = [];

		StompClient.subscribe($scope, '/event/import/' + $scope.importSettings.id, function (message) {
			var body = JSON.parse(message.body);
			$scope.progress = 100.0 * body["currentBoard"] / body["boards"];
			$scope.currentBoardName = body["boardName"];
		});

		$scope.checkShortName = function (board) {
			Board.checkShortName(board.shortName).then(function (res) {
				board.checkedShortName = res;
			});
		};

		$scope.cancel = function () {
			$scope.availableOrganizations = [];
			$scope.progress = 0;
			$scope.currentBoardName = undefined;
		};


		Admin.findByKey('TRELLO_API_KEY').then(function (key) {
			if (key.second == null) {
				$scope.hasApiKey = false;
				return;
			}

			$scope.hasApiKey = true;

			var trelloApiKey = key.second;
			var trelloSecret = '';
			$scope.trelloClientUrl = 'https://api.trello.com/1/client.js?key=' + trelloApiKey;

			$scope.importFromTrello = function () {
				$scope.trelloImportIsRunning = true;
				$scope.progress = 0;
				$scope.currentBoardName = undefined;
				var boardsToImport = [];

				angular.forEach($scope.availableOrganizations, function (o) {
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
					projectShortName: $scope.project.shortName,
					importId: $scope.importSettings.id,
					importArchived: $scope.importSettings.archived,
					boards: boardsToImport
				}).then(function () {
					$scope.trelloImportIsRunning = false;
					Notification.addAutoAckNotification('success', {key: 'notification.project-manage-import.importFromTrello.success'}, false);
				});
			};

			$scope.connectToTrello = function () {
				$scope.connectingToTrello = true;
				Trello.authorize({
					type: "popup",
					name: "Trello Importer",
					expiration: "1day",
					persist: false,
					scope: {read: true, write: false, account: true},
					success: function () {
						$scope.$apply(function () {
							trelloSecret = Trello.token();
							Project.getAvailableTrelloBoards({
								apiKey: trelloApiKey,
								secret: trelloSecret
							}).then(function (result) {
								$scope.connectingToTrello = false;
								$scope.availableOrganizations = result.organizations;
							});
						});
					},
					error: function () {
						$scope.$apply(function () {
							$scope.connectingToTrello = false;
							Notification.addAutoAckNotification('error', {key: 'notification.project-manage-import.importFromTrello.error'}, false);
						});
					}
				});
			}
		});

	});
})();