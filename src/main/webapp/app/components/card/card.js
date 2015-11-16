(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgComponentCard', CardComponent);

    function CardComponent(CardCache, Card, User, LabelCache, Label, StompClient, Notification, Board, BulkOperations) {
        return {
            restrict: 'E',
            templateUrl: 'app/components/card/card.html',
            scope: true,
            bindToController: {
                project: '=',
                board: '=',
                card: '=',
                app: '='
            },
            controller: CardController,
            controllerAs: 'cardCtrl'
        }
    }

    function CardController($scope, $rootScope, $timeout, CardCache, Card, User, LabelCache, Label, StompClient,
        Notification, Board, BulkOperations) {
        var ctrl = this;
        var board = ctrl.board;
        var project = ctrl.project;
        var card = ctrl.card;

        ctrl.view = {};

        var findAndAssignColumns = function() {
            Board.columns(board.shortName, 'BOARD').then(function(columns) {
                ctrl.columns = columns;
            });
        };

        StompClient.subscribe($scope, '/event/board/'+board.shortName+'/location/BOARD/column', findAndAssignColumns);

        findAndAssignColumns();
        //

        ctrl.notSameColumn = function(col) {
            return col.id != ctrl.card.columnId;
        };

        ctrl.notEmptyColumn = function(model) {
            return model != "" && model != null;
        }
        //

        var $scopeForColumn = undefined;

        var loadColumn = function(columnId) {
            Board.column(columnId).then(function(col) {
                ctrl.column = col;
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
            ctrl.app.setTitle('[' + board.shortName + '-' + card.sequence + '] ' + card.name + ' - Lavagna');
        };
        refreshTitle();
        //------------------

        var reloadCard = function() {
            CardCache.card(card.id).then(function(c) {
                ctrl.card = c;
                refreshTitle();
                loadColumn(c.columnId);
                card = ctrl.card;
            });
            loadActivity();
        };

        var unbindCardCache = $rootScope.$on('refreshCardCache-' + card.id, reloadCard);
        $scope.$on('$destroy', unbindCardCache);

        var loadDescription = function() {
            Card.description(card.id).then(function(description) {
                ctrl.description = description;
            });
        };

        loadDescription();

        ctrl.updateDescription = function(description) {
            Card.updateDescription(card.id, description).then(function() {
                description.content = null;
            });
        };

        var loadComments = function() {
            Card.comments(card.id).then(function(comments) {
                ctrl.comments = comments;
            });
        };

        loadComments();

        ctrl.labelNameToId = {};

        var loadLabel = function() {
            LabelCache.findByProjectShortName(project.shortName).then(function(labels) {
                ctrl.labels = labels;
                ctrl.userLabels = {};
                ctrl.labelNameToId = {};
                for(var k in labels) {
                    ctrl.labelNameToId[labels[k].name] = k;
                    if(labels[k].domain === 'USER') {
                        ctrl.userLabels[k] = labels[k];
                    }
                }
            });
        };
        loadLabel();

        var unbind = $rootScope.$on('refreshLabelCache-' + project.shortName, loadLabel);
        $scope.$on('$destroy', unbind);

        var loadLabelValues = function() {
            Label.findValuesByCardId(card.id).then(function(labelValues) {
                ctrl.labelValues = labelValues;
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
                ctrl.actionLists = [];
                ctrl.actionListsById = {};
                ctrl.actionListsId = [];
                for(var i = 0; i < actionLists.lists.length; i++) {
                    ctrl.actionListsId.push(actionLists.lists[i].id);
                    ctrl.actionListsById[actionLists.lists[i].id] = actionLists.lists[i];
                    ctrl.actionLists.push(actionLists.lists[i]);
                }

                ctrl.actionItems = actionLists.items;
                ctrl.actionItemsMap = {};
                ctrl.actionListsStats = {};
                for(var i = 0; i < ctrl.actionListsId.length; i++) {
                    var currentListID = ctrl.actionListsId[i];
                    //there could be a list without elements
                    ctrl.actionListsStats[currentListID] = 0;
                    if(actionLists.items[currentListID] === undefined)
                        continue;
                    var checkedItems = 0;
                    for(var e = 0; e < actionLists.items[currentListID].length; e++) {
                        var item = actionLists.items[currentListID][e];
                        if(item.type === 'ACTION_CHECKED')
                            checkedItems++;
                        ctrl.actionItemsMap[item.id] = item;
                    }
                   ctrl.actionListsStats[currentListID] = parseInt((checkedItems/actionLists.items[currentListID].length) * 100, 10);
                }
            });
        };
        loadActionLists();

        User.hasPermission('MANAGE_ACTION_LIST', project.shortName).then(function() {
            ctrl.sortableActionListOptions = {
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
            ctrl.sortableActionListOptions = false;
        })

        User.hasPermission('MANAGE_ACTION_LIST', project.shortName).then(function() {
            ctrl.sortableActionItemsOptions = {
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
            ctrl.sortableActionItemsOptions = false;
        });
        //

        //--------------

        ctrl.actionListState = {};

        ctrl.addComment = function(comment) {
            Card.addComment(card.id, comment).then(function() {
                comment.content = null;
            });
        };

        ctrl.addActionList = function(actionList) {
            Card.addActionList(card.id, actionList).then(function() {
                actionList.content = null;
            });
        };

        ctrl.deleteActionList = function(itemId) {
            Card.deleteActionList(itemId).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.ACTION_LIST_DELETE.success'}, true, function(notification) {
                    Card.undoDeleteActionList(event.id).then(notification.acknowledge)
                });
            }, function(error) {
                ctrl.actionListState[listId].deleteList = false;
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_LIST_DELETE.success'}, false);
            });
        };

        ctrl.saveActionList = function(itemId, content) {
            Card.updateActionList(itemId, content);
        };

        ctrl.addActionItem = function(listId, actionItem) {
            Card.addActionItem(listId, actionItem).then(function() {
                actionItem.content = null;
            });
        };

        ctrl.deleteActionItem = function(listId, itemId) {
            Card.deleteActionItem(itemId).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.ACTION_ITEM_DELETE.success'}, true, function(notification) {
                    Card.undoDeleteActionItem(event.id).then(notification.acknowledge);
                });
            }, function(error) {
                ctrl.actionListState[listId][itemId].deleteActionItem = false;
                ctrl.actionListState[listId][itemId].showControls = false;
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
            });
        };

        ctrl.toggleActionItem = function(itemId) {
            Card.toggleActionItem(itemId, (ctrl.actionItemsMap[itemId].type === 'ACTION_UNCHECKED'));
        };

        ctrl.saveActionItem = function(itemId, content) {
            Card.updateActionItem(itemId, content);
        };

        // -----
        ctrl.updateCardName = function(newName) {
            Card.update(card.id, newName);
        };
        //
        ctrl.updateComment = function(comment, commentToEdit) {
            Card.updateComment(comment.id, commentToEdit);
        };
        ctrl.deleteComment = function(comment, control) {
            Card.deleteComment(comment.id).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.COMMENT_DELETE.success'}, true, function(notification) {
                    Card.undoDeleteComment(event.id).then(notification.acknowledge)
                });
            }, function(error) {
                control = false;
                Notification.addAutoAckNotification('error', { key : 'notification.card.ACTION_ITEM_DELETE.error'}, false);
            });
        };

        // labels

        ctrl.hasUserLabels = function(userLabels, labelValues) {
            if(userLabels === undefined || labelValues === undefined) {
                return false;
            }
            var count = 0;
            for(var n in userLabels) {
                count += labelValues[n] === undefined ? 0 : labelValues[n].length;
            }
            return count > 0;
        }

        var currentCard = function() {
            var cardByProject = {};
            cardByProject[project.shortName] = [card.id];
            return cardByProject;
        };

        ctrl.removeLabelValue = function(label) {

            BulkOperations.removeLabel(currentCard(), {id: label.labelId}, label.value).then(function(data) {

                    Notification.addAutoAckNotification('success', {
                        key : 'notification.card.LABEL_DELETE.success',
                        parameters : {
                            labelName : ctrl.userLabels[label.labelId].name }
                    }, false);

            }, function(error) {
                ctrl.actionListState[listId].deleteList = false;

                    Notification.addAutoAckNotification('error', {
                        key : 'notification.card.LABEL_DELETE.error',
                        parameters : {
                            labelName : ctrl.userLabels[label.labelId].name }
                    }, false);

            })
        };

        ctrl.findElementWithUserId = function(arr, val) {
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



        ctrl.setDueDate = function(date) {
            BulkOperations.setDueDate(currentCard(), date)
        };

        ctrl.removeDueDate = function() {
            BulkOperations.removeDueDate(currentCard())
        };

        ctrl.watchCard = function(user) {
            BulkOperations.watch(currentCard(), user);
        };

        ctrl.unWatchCard = function(user) {
            BulkOperations.unWatch(currentCard(), user);
        };

        ctrl.assignToUser = function(user) {
            BulkOperations.assign(currentCard(), user);
        };

        ctrl.removeAssignForUser = function(user) {
            BulkOperations.removeAssign(currentCard(), user);
        };

        ctrl.setMilestone = function(milestone) {
            BulkOperations.setMilestone(currentCard(), milestone);
        }

        ctrl.removeMilestone = function() {
            BulkOperations.removeMilestone(currentCard());
        }

        ctrl.addNewLabel = function(labelToAdd) {
            var labelValueToUpdate = Label.extractValue(labelToAdd.label, labelToAdd.value);
            BulkOperations.addLabel(currentCard(), labelToAdd.label, labelValueToUpdate)
        };

        //-- file upload
        var loadFiles = function() {
            Card.files(card.id).then(function(files) {
                ctrl.files = {};
                for(var f = 0; f < files.length; f++) {
                    var file = files[f];
                    ctrl.files[file.cardDataId] = file;
                }
            });
        };

        loadFiles();

        var processUploadingFile = function(status) {
            $timeout(function() {
                var index = ctrl.filesToUpload.indexOf(ctrl.uploadingFile);
                ctrl.uploadingFile = null;
                ctrl.filesToUpload[index].status = status;
            });
        };


        var fileUploadProgressCallBack = function(event) {
            if (event.lengthComputable && event.loaded != event.total) { // prevent null reference
                ctrl.uploadingFile['progress'] = Math.round(event.loaded * 100 / event.total);
            }
        };

        var fileUploadCompleteCallBack = function(data, status) {
            if(status == 200) {
                loadFiles();

                var uploadedFile = ctrl.uploadingFile;
                ctrl.uploadingFile = null;
                ctrl.filesToUpload.splice(ctrl.filesToUpload.indexOf(uploadedFile),1);
            } else {
                processUploadingFile('failed');
            }

        };

        var fileUploadFailedCallBack = function(event) {
            processUploadingFile('failed');
        };

        var fileUploadCanceledCallBack = function() {
        };

        ctrl.filesToUpload = [];
        ctrl.uploadingFile = null;

        ctrl.addFiles = function($files) {
            $scope.$apply(function() {
                for (var i = 0; i < $files.length; i++) {
                    ctrl.filesToUpload.push({
                        xhr : null,
                        progress : 0,
                        file : $files[i],
                        status : 'queue'});
                }
            });
        };

        function getNextFileToUpload(_files) {
            for(var f = 0; f < _files.length; f++) {
                var file = _files[f];
                if(file.status == 'queue') {
                    return file;
                }
            }
            return null;
        }

        ctrl.uploadNextFile = function() {
            $timeout(function() {
                Card.getMaxFileSize().then(function(size) {
                    var uploadingFile = getNextFileToUpload(ctrl.filesToUpload);

                    if(uploadingFile == null) {
                        return;
                    }

                    ctrl.uploadingFile = uploadingFile;

                    var maxSize = size === "" ? Math.NaN : parseInt(JSON.parse(size));
                    if(!isNaN(maxSize) && uploadingFile.file.size > maxSize) {
                        Notification.addNotification('error', {key : 'notification.error.file-size-too-big', parameters: {maxSize :maxSize}}, false);
                        processUploadingFile('failed');
                        return;
                    }

                    ctrl.uploadingFile.status = 'upload';
                    ctrl.uploadingFile.xhr = Card.uploadFile(uploadingFile.file, card.id, fileUploadProgressCallBack, fileUploadCompleteCallBack,
                                fileUploadFailedCallBack, fileUploadCanceledCallBack);
                });
            });
        };

        ctrl.abortUpload = function() {
            ctrl.uploadingFile.xhr.abort();
        };

        ctrl.cancelUpload = function(elementIndex) {
            ctrl.filesToUpload.splice(elementIndex, 1);
        };

        ctrl.retryUpload = function(elementIndex) {
            $timeout(function() {
                ctrl.filesToUpload[elementIndex].xhr = null;
                ctrl.filesToUpload[elementIndex].progress = 0;
                ctrl.filesToUpload[elementIndex].status = 'queue';
            });
        };

        ctrl.deleteFile = function(dataId) {
            Card.deleteFile(dataId).then(function(event) {
                Notification.addNotification('success', {key : 'notification.card.FILE_DELETE.success'}, true, function(notification) {
                    Card.undoDeleteFile(event.id).then(notification.acknowledge);
                });
            }, function(error) {
                ctrl.fileControls[dataId].deleteFile = false;
                Notification.addAutoAckNotification('error', {key : 'notification.card.FILE_DELETE.error'}, false);
            });
        };

        $scope.$watch('filesToUpload', function() {
            if(ctrl.filesToUpload.length > 0 && ctrl.uploadingFile == null) {
                ctrl.uploadNextFile();
            }
        }, true);

        //activity
        var loadActivity = function() {
            Card.activity(card.id).then(function(activities) {
                ctrl.activities = activities;
            });
        };

        loadActivity();

        ctrl.hasActivity = function() {
            return (ctrl.comments !== undefined && ctrl.actionLists !== undefined && ctrl.actionItems !== undefined && ctrl.files !== undefined);
        };

        ctrl.moveCard = function(location) {
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

        ctrl.moveToColumn = function(toColumn) {
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
    }
})();
