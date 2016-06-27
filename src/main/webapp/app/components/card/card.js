(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentCard', {
        templateUrl: 'app/components/card/card.html',
        bindings: {
            project: '=',
            board: '=',
            card: '=',
            user: '='
        },
        controller: CardController
    });

    function CardController($scope, $rootScope, $timeout, CardCache, Card, User, LabelCache, Label, StompClient,
        Notification, Board, BulkOperations, Title) {
        var ctrl = this;
        var board = ctrl.board;
        var project = ctrl.project;
        var card = ctrl.card;

        ctrl.view = {};

        //------------------

        function refreshTitle() {
        	Title.set('title.card', { shortname: board.shortName, sequence: ctrl.card.sequence, name: ctrl.card.name });
        }

        var reloadCard = function() {
            CardCache.card(card.id).then(function(c) {
                ctrl.card = c;
                card = ctrl.card;
                refreshTitle();
            });
            loadActivity();
        };

        var unbindCardCache = $rootScope.$on('refreshCardCache-' + card.id, reloadCard);
        $scope.$on('$destroy', unbindCardCache);

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

        //----------------------
        var loadActionListsAndRefresh = function() {
            loadActionLists();
            reloadCard();
        };

        var loadActionLists = function() {
            Card.actionLists(card.id).then(function(actionLists) {
                ctrl.actionLists = [];

                for(var i = 0; i < actionLists.lists.length; i++) {
                    var actionList = actionLists.lists[i];
                    actionList.items = actionLists.items[actionList.id] || [];
                    actionList.items.referenceId = actionList.id;
                    ctrl.actionLists.push(actionList);
                }
            });
        };
        loadActionLists();

        ctrl.sortActionLists = function(actionlist, from, to, oldIndex, newIndex) {
            ctrl.actionLists = to.map(function(v, i) {
                    v.order = i;
                    return v;
                });
            Card.updateActionListOrder(
                ctrl.card.id,
                to.map(function(v) { return v.id})
                ).catch(function(err) {
                    ctrl.actionLists = from;
                });
        };

        ctrl.addActionList = function(actionList) {
            Card.addActionList(card.id, actionList).then(function() {
                actionList = null;
            });
        };

        //

        //--------------

        ctrl.addComment = function(comment) {
            Card.addComment(card.id, comment).then(function() {
                comment.content = null;
            });
        };

        //
        ctrl.updateComment = function(comment, commentToEdit) {
            Card.updateComment(comment.id, commentToEdit);
        };
        ctrl.deleteComment = function(comment, control) {
            Card.deleteComment(comment.id).then(function(event) {
                Notification.addNotification('success', { key : 'notification.card.COMMENT_DELETE.success'}, true, true, function(notification) {
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
        };

        var currentCard = function() {
            var cardByProject = {};
            cardByProject[project.shortName] = [card.id];
            return cardByProject;
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

        ctrl.watchCard = function(user) {
            BulkOperations.watch(currentCard(), user);
        };

        ctrl.unWatchCard = function(user) {
            BulkOperations.unWatch(currentCard(), user);
        };

        ctrl.searchUser = function(text) {
            return User.findUsers(text.trim()).then(function (res) {
                angular.forEach(res, function(user) {
                    user.label = User.formatName(user);
                });
                return res;
            });
        };

        ctrl.assignUser = function(user) {
            if(user === undefined || user === null) {
                return;
            }
            BulkOperations.assign(currentCard(), user);
        }

        ctrl.removeAssignForUser = function(user) {
            BulkOperations.removeAssign(currentCard(), {id: user.value.valueUser});
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
            $timeout(function() {
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
                        Notification.addNotification('error', {key : 'notification.error.file-size-too-big', parameters: {maxSize :maxSize}}, true, false);
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
                Notification.addNotification('success', {key : 'notification.card.FILE_DELETE.success'}, true, true, function(notification) {
                    Card.undoDeleteFile(event.id).then(notification.acknowledge);
                });
            }, function(error) {
                ctrl.fileControls[dataId].deleteFile = false;
                Notification.addAutoAckNotification('error', {key : 'notification.card.FILE_DELETE.error'}, false);
            });
        };

        $scope.$watchCollection('cardCtrl.filesToUpload', function() {
            if(ctrl.filesToUpload.length > 0 && ctrl.uploadingFile == null) {
                ctrl.uploadNextFile();
            }
        });

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


        // ----
        var loadLabelValues = function() {
            Label.findValuesByCardId(card.id).then(function(labelValues) {
                ctrl.labelValues = labelValues;
            });
        };
        loadLabelValues();

        var loadActionLists = function() {
            Card.actionLists(card.id).then(function(actionLists) {
                ctrl.actionLists = [];

                for(var i = 0; i < actionLists.lists.length; i++) {
                    var actionList = actionLists.lists[i];
                    actionList.items = actionLists.items[actionList.id] || [];
                    actionList.items.referenceId = actionList.id;
                    ctrl.actionLists.push(actionList);
                }
            });
        };
        loadActionLists();
        // ----

        //the /card-data has various card data related event that are pushed from the server that we must react
        StompClient.subscribe($scope, '/event/card/' + card.id + '/card-data', function(e) {
            var type = JSON.parse(e.body).type;
            if(type.match(/COMMENT/g) !== null) {
                loadComments();
                reloadCard();
            } else if(type.match(/ACTION_ITEM$/g) !== null || type.match(/ACTION_LIST$/g)) {
                loadActionLists();
                reloadCard();
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
