(function() {

	//FIXME, TODO: clean up the label part, it's a wreck

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('CardCtrl', function($stateParams, $scope, $rootScope, $filter, $timeout,
			CardCache, Card, User, LabelCache, Label, StompClient, Notification, Board,
			card, currentUser, project, board //resolved by ui-router
			) {
		
		
		var findAndAssignColumns = function() {
			Board.columns(board.shortName, 'BOARD').then(function(columns) {
				$scope.columns = columns;
			});
		};
		
		StompClient.subscribe($scope, '/event/board/'+board.shortName+'/location/BOARD/column', findAndAssignColumns);
		
		findAndAssignColumns();

		$scope.codeMirrorOptions = {
				lineWrapping : true,
		        lineNumbers: true,
		        mode: 'markdown',
		        viewportMargin: Infinity
		};

		$scope.currentUser = currentUser;
		$scope.project = project;
		$scope.board = board;
		$scope.card = card;
		
		//
		
		$scope.notSameColumn = function(col) {
			return col.id != $scope.card.columnId;
		};
		
		$scope.notEmptyColumn = function(model) {
			return model != "" && model != null;
		}
		//
		
		var $scopeForColumn = undefined;
		
		var loadColumn = function(columnId) {
			Board.column(columnId).then(function(col) {
				$scope.column = col;
			});
			
			if($scopeForColumn) {
				$scopeForColumn.$destroy();
			}
			
			$scopeForColumn = $scope.$new()
			
			//subscribe on any change to the column
			StompClient.subscribe($scopeForColumn, '/event/column/' + columnId, function() {
				loadColumn(columnId);
			});
			
		};
		loadColumn(card.columnId);
		//

		var refreshTitle = function() {
			$rootScope.pageTitle = '[' + board.shortName + '-' + $scope.card.sequence + '] ' + $scope.card.name + ' - Lavagna';
		};
		refreshTitle();
		//------------------

		var reloadCard = function() {
			CardCache.card(card.id).then(function(c) {
				$scope.card = c;
				refreshTitle();
				loadColumn(c.columnId);
			});
			loadActivity();
		};

		var unbindCardCache = $rootScope.$on('refreshCardCache-' + card.id, reloadCard);
		$scope.$on('$destroy', unbindCardCache);

		var loadDescription = function() {
			Card.description(card.id).then(function(description) {
				$scope.description = description;
			});
		};

		loadDescription();

		$scope.updateDescription = function(description) {
			Card.updateDescription(card.id, description).then(function() {
				description.content = null;
			});
		};

		var loadComments = function() {
			Card.comments(card.id).then(function(comments) {
				$scope.comments = comments;
			});
		};

		loadComments();

		$scope.labelNameToId = {};

		var loadLabel = function() {
			LabelCache.findByProjectShortName(project.shortName).then(function(labels) {
				$scope.labels = labels;
				$scope.userLabels = {};
				$scope.labelNameToId = {};
				for(var k in labels) {
					$scope.labelNameToId[labels[k].name] = k;
					if(labels[k].domain === 'USER') {
						$scope.userLabels[k] = labels[k];
					}
				}
			});
		};
		loadLabel();

		var unbind = $rootScope.$on('refreshLabelCache-' + project.shortName, loadLabel);
		$scope.$on('$destroy', unbind);

		var loadLabelValues = function() {
			Label.findValuesByCardId(card.id).then(function(labelValues) {
				$scope.labelValues = labelValues;
			});
		};
		loadLabelValues();

		//----------------------
		var loadActionListsAndRefresh = function() {
			loadActionLists();
			reloadCard();
		};

		var loadActionLists = function() {
			Card.actionLists(card.id).then(function(actionLists) {
				$scope.actionLists = [];
				$scope.actionListsById = {};
				$scope.actionListsId = [];
				for(var i = 0; i < actionLists.lists.length; i++) {
					$scope.actionListsId.push(actionLists.lists[i].id);
					$scope.actionListsById[actionLists.lists[i].id] = actionLists.lists[i];
					$scope.actionLists.push(actionLists.lists[i]);
				}

				$scope.actionItems = actionLists.items;
				$scope.actionItemsMap = {};
				$scope.actionListsStats = {};
				for(var i = 0; i < $scope.actionListsId.length; i++) {
					var currentListID = $scope.actionListsId[i];
					//there could be a list without elements
					$scope.actionListsStats[currentListID] = 0;
					if(actionLists.items[currentListID] === undefined)
						continue;
					var checkedItems = 0;
					for(var e = 0; e < actionLists.items[currentListID].length; e++) {
						var item = actionLists.items[currentListID][e];
						if(item.type === 'ACTION_CHECKED')
							checkedItems++;
						$scope.actionItemsMap[item.id] = item;
					}
					$scope.actionListsStats[currentListID] = parseInt((checkedItems/actionLists.items[currentListID].length) * 100, 10);
				}
			});
		};
		loadActionLists();

		User.hasPermission('MANAGE_ACTION_LIST', $stateParams.projectName).then(function() {
			$scope.sortableActionListOptions = {
					items: '> .lavagna-sortable-card-actionlist',
					cancel: 'input,textarea,button,select,option,.lavagna-not-sortable-card-actionlist',
					start : function(e, ui) {},
					stop : function(e, ui) {},
					update : function(e, ui) {
						var newActionListsId = ui.item.parent().sortable("toArray", {attribute: 'data-lavagna-actionlist-id'}).map(function(i) {return parseInt(i, 10);});
						Card.updateActionListOrder(card.id, newActionListsId);
					}
			};
		}, function() {
			$scope.sortableActionListOptions = false;
		})

		User.hasPermission('MANAGE_ACTION_LIST', $stateParams.projectName).then(function() {
			$scope.sortableActionItemsOptions = {
					connectWith: ".lavagna-card-actionitems",
					start:function(e, ui) {
						ui.item.data('initialActionlistId', ui.item.parent().parent().parent().attr('data-lavagna-actionlist-id'));
					},
					stop: function(e, ui) {
						if(ui.item.data('hasUpdate')) {
							var itemId = parseInt(ui.item.attr('data-lavagna-actionlistitem-id'), 10);
							var oldActionlistId = parseInt(ui.item.data('initialActionlistId'), 10);
							var newActionlistId = parseInt(ui.item.data('newActionlistId'), 10);
							var ids = ui.item.parent().sortable("toArray", {attribute: 'data-lavagna-actionlistitem-id'}).map(function(i) {return parseInt(i, 10);});
							if(oldActionlistId === newActionlistId) {
								Card.updateActionItemOrder(oldActionlistId, ids);
							} else {
								Card.moveActionItem(itemId, newActionlistId, {newContainer: ids});
							}
						}
						ui.item.removeData('hasUpdate');
						ui.item.removeData('initialActionlistId');
						ui.item.removeData('newActionlistId');
					},
					update : function(e, ui) {
						ui.item.data('newActionlistId', ui.item.parent().parent().parent().attr('data-lavagna-actionlist-id'));
						ui.item.data('hasUpdate', true);
					}
			};
		}, function() {
			$scope.sortableActionItemsOptions = false;
		});
		//

		//--------------

		$scope.actionListState = {};

		$scope.addComment = function(comment) {
			Card.addComment(card.id, comment).then(function() {
				comment.content = null;
			});
		};

		$scope.addActionList = function(actionList) {
			Card.addActionList(card.id, actionList).then(function() {
				actionList.content = null;
			});
		};

		$scope.deleteActionList = function(itemId) {
			Card.deleteActionList(itemId).then(function(data) {
				Notification.addNotification('success', { key : 'notification.card.ACTION_LIST_DELETE.success'}, true, function(notification) {
					Card.undoDeleteActionList(notification.event.id).then(notification.acknowledge)
				});
			}, function(error) {
				$scope.actionListState[listId].deleteList = false;
				Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_LIST_DELETE.success'}, false);
			});
		};

		$scope.saveActionList = function(itemId, content) {
			Card.updateActionList(itemId, content);
		};

		$scope.addActionItem = function(listId, actionItem) {
			Card.addActionItem(listId, actionItem).then(function() {
				actionItem.content = null;
			});
		};

		$scope.deleteActionItem = function(listId, itemId) {
			Card.deleteActionItem(itemId).then(function(data) {
				Notification.addNotification('success', { key : 'notification.card.ACTION_ITEM_DELETE.success'}, true, function(notification) {
					Card.undoDeleteActionItem(notification.event.id).then(notification.acknowledge);
				});
			}, function(error) {
				$scope.actionListState[listId][itemId].deleteActionItem = false;
				$scope.actionListState[listId][itemId].showControls = false;
				Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
			});
		};

		$scope.toggleActionItem = function(itemId) {
			Card.toggleActionItem(itemId, ($scope.actionItemsMap[itemId].type === 'ACTION_UNCHECKED'));
		};

		$scope.saveActionItem = function(itemId, content) {
			Card.updateActionItem(itemId, content);
		};

		// -----
		$scope.updateCardName = function(card, newName) {
			Card.update(card.id, newName);
		};
		//
		$scope.updateComment = function(comment, commentToEdit) {
			Card.updateComment(comment.id, commentToEdit);
		};
		$scope.deleteComment = function(comment, control) {
			Card.deleteComment(comment.id).then(function(data) {
				Notification.addNotification('success', { key : 'notification.card.COMMENT_DELETE.success'}, true, function(notification) {
					Card.undoDeleteComment(notification.event.id).then(notification.acknowledge)
				});
			}, function(error) {
				control = false;
				Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
			});
		};

		// labels
		
		$scope.hasUserLabels = function(userLabels, labelValues) {
			if(userLabels === undefined || labelValues === undefined) {
				return false;
			}
			var count = 0;
			for(var n in userLabels) {
				count += labelValues[n] === undefined ? 0 : labelValues[n].length;
			}
			return count > 0;
		}

		$scope.removeLabelValue = function(label) {
			
			LabelCache.findLabelByProjectShortNameAndId(project.shortName, label.labelId).then(function(data) {
				return data.domain === 'USER';
			}).then(function(notify) {
				//we only notify for USER label removal
				Label.removeValue(card.id, label.cardLabelValueId).then(function(data) {
					if(notify) {
						Notification.addAutoAckNotification('success', { 
							key : 'notification.card.LABEL_DELETE.success', 
							parameters : { 
								labelName : $scope.userLabels[label.labelId].name }
						}, false);
					}
				}, function(error) {
					$scope.actionListState[listId].deleteList = false;
					if(notify) {
						Notification.addAutoAckNotification('error', { 
							key : 'notification.card.LABEL_DELETE.error', 
							parameters : { 
								labelName : $scope.userLabels[label.labelId].name }
						}, false);
					}
				})
			})
		};

		$scope.findElementWithUserId = function(arr, val) {
			if(!angular.isArray(arr)) {
				return false;
			}
			for(var i = 0; i < arr.length; i++) {
				if(arr[i].value.valueUser === val) {
					return arr[i];
				}
			}
			return false;
		};

		$scope.updateLabelValue = function(label, prevValues, value) {
			var labelValueToUpdate = Label.extractValue(label, value);
			if(prevValues === undefined) {
				Label.addValueToCard(card.id, label.id, labelValueToUpdate);
			} else {
				Label.updateValue(card.id, prevValues[0].cardLabelValueId, labelValueToUpdate);
			}
		};

		$scope.addNewLabel = function(labelToAdd) {
			$scope.updateLabelValue(labelToAdd.label, undefined, labelToAdd.value);
		};

		//-- file upload
		var loadFiles = function() {
			Card.files(card.id).then(function(files) {
				$scope.files = {};
				for(var f = 0; f < files.length; f++) {
					var file = files[f];
					$scope.files[file.cardDataId] = file;
				}
			});
		};

		loadFiles();

		var processUploadingFile = function(icon, status) {
			$timeout(function() {
				$scope.processedFiles.push({'icon': icon, 'status': status, 'file': $scope.uploadingFiles['file']});
				$scope.uploadingFile = null;
			});
		};


		var fileUploadProgressCallBack = function(event) {
			if (event.lengthComputable) {
				$scope.uploadingFile['progress'] = Math.round(event.loaded * 100 / event.total);
			}
		};

		var fileUploadCompleteCallBack = function(data, status) {
			if(status == 200) {
				$scope.uploadingFile = null;
			} else {
				processUploadingFile('warning', 'failed');
			}

		};

		var fileUploadFailedCallBack = function() {
			processUploadingFile('warning', 'failed');
		};

		var fileUploadCanceledCallBack = function() {
			processUploadingFile('stop', 'aborted by user');
		};

		$scope.filesToUpload = [];
		$scope.uploadingFile = null;
		$scope.processedFiles = [];

		$scope.addFiles = function($files) {
			$scope.$apply(function() {
				for (var i = 0; i < $files.length; i++) {
					$scope.filesToUpload.push($files[i]);
				}
			});
		};

		$scope.uploadNextFile = function() {
			$timeout(function() {
				Card.getMaxFileSize().then(function(size) {
					var file = $scope.filesToUpload.shift();
					var maxSize = size === "" ? Math.NaN : parseInt(JSON.parse(size));
					if(!isNaN(maxSize) && file.size > maxSize) {
						Notification.addNotification('error', {key : 'notification.error.file-size-too-big', parameters: {maxSize :maxSize}}, false);
						return;
					}
					
					$scope.uploadingFile = {
						'xhr': Card.uploadFile(file, card.id, fileUploadProgressCallBack, fileUploadCompleteCallBack,
								fileUploadFailedCallBack, fileUploadCanceledCallBack),
						'progress': 0,
						'file' : file
					};
				});
			});
		};

		$scope.abortUpload = function() {
			$scope.uploadingFile['xhr'].abort();
		};

		$scope.cancelUpload = function(elementIndex) {
			$scope.processedFiles.push({'icon': 'stop', 'status': 'aborted by user', 'file': $scope.filesToUpload.splice(elementIndex, 1)[0]});
		};

		$scope.clearFileUploadResultQueue = function() {
			$scope.processedFiles = [];
		};


		$scope.retryUpload = function(elementIndex) {
			$timeout(function() {
				$scope.$apply(function() {
					$scope.filesToUpload.push($scope.processedFiles.splice(elementIndex, 1)[0]['file']);
				});
			});
		};

		$scope.deleteFile = function(dataId) {
			Card.deleteFile(dataId).then(function(data) {
				Notification.addNotification('success', {key : 'notification.card.FILE_DELETE.success'}, true, function(notification) {
					Card.undoDeleteFile(notification.event.id).then(notification.acknowledge);
				});
			}, function(error) {
				$scope.fileControls[dataId].deleteFile = false;
				Notification.addAutoAckNotification('error', {key : 'notification.card.FILE_DELETE.error'}, false);
			});
		};

		$scope.$watchCollection('uploadingFile', function() {
			if($scope.filesToUpload.length > 0 && $scope.uploadingFile == null) {
				$scope.uploadNextFile();
			}
		});

		$scope.$watchCollection('filesToUpload', function() {
			if($scope.filesToUpload.length > 0 && $scope.uploadingFile == null) {
				$scope.uploadNextFile();
			}
		});

		//activity
		var loadActivity = function() {
			Card.activity(card.id).then(function(activities) {
				$scope.activities = activities;
			});
		};

		loadActivity();

		$scope.hasActivity = function() {
			return ($scope.comments !== undefined && $scope.actionLists !== undefined && $scope.actionItems !== undefined && $scope.files !== undefined);
		};
		
		$scope.moveCard = function(card, location) {
			Card.moveAllFromColumnToLocation(card.columnId, [card.id], location).then(reloadCard).then(function() {
				Notification.addAutoAckNotification('success', {
					key: 'notification.card.moveToLocation.success',
					parameters: { location: location }
				}, false);
			}, function(error) {
				Notification.addAutoAckNotification('error', {
					key: 'notification.card.moveToLocation.error',
					parameters: { location: location }
				}, false);
			});
		};
		
		$scope.moveToColumn = function(card, toColumn) {
			if(angular.isUndefined(toColumn)) {
				return;
			}

			Card.findByColumn(toColumn.id).then(function(cards) {
				var ids = [];
				for (var i = 0;i<cards.length;i++) {
					ids.push(cards[i].id);
				}

				ids.push(card.id);

				return ids;
			}).then(function(ids) {
				Board.moveCardToColumn(card.id, card.columnId, toColumn.id, {newContainer: ids}).then(function() {
					Notification.addAutoAckNotification('success', {
						key: 'notification.card.moveToColumn.success',
						parameters: { columnName: toColumn.name }
					}, false);
				}, function(error) {
					Notification.addAutoAckNotification('error', {
						key: 'notification.card.moveToColumn.error',
						parameters: { columnName: toColumn.name }
					}, false);
				})
			});
		}


		//the /card-data has various card data related event that are pushed from the server that we must react
		StompClient.subscribe($scope, '/event/card/' + card.id + '/card-data', function(e) {
			var type = JSON.parse(e.body).type;
			if(type === 'UPDATE_DESCRIPTION') {
				loadDescription();
				reloadCard();
			} else if(type.match(/COMMENT/g) !== null) {
				loadComments();
				reloadCard();
			} else if(type.match(/ACTION_ITEM$/g) !== null || type.match(/ACTION_LIST$/g)) {
				loadActionListsAndRefresh();
			} else if(type.indexOf('LABEL') > -1) {
				loadLabelValues();
				reloadCard();
			} else if(type.match(/FILE$/g)) {
				loadFiles();
				reloadCard();
			}
		});
	});

})();