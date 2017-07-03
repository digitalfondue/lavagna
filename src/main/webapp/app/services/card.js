(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data;
    };

    var extractActionLists = function (data) {
        var rawData = data.data;
        var actionLists = {lists: [], items: {}};

        for (var i = 0; i < rawData.length; i++) {
            // if it's a list, push it to the array
            if (rawData[i].type === 'ACTION_LIST') { actionLists.lists.push(rawData[i]); }
            // if it's an item, check if the array already exists, and then push into it
            if (rawData[i].type === 'ACTION_CHECKED' || rawData[i].type === 'ACTION_UNCHECKED') {
                if (actionLists.items[rawData[i].referenceId] === undefined) { actionLists.items[rawData[i].referenceId] = []; }
                actionLists.items[rawData[i].referenceId].push(rawData[i]);
            }
        }

        return actionLists;
    };

    var isInCardLabels = function (cardLabels, labelName, currentUserId) {
        if (cardLabels === undefined || cardLabels.length === 0) { return false; } // empty, no labels at all
        for (var i = 0; i < cardLabels.length; i++) {
            if (cardLabels[i].labelName === labelName && cardLabels[i].labelDomain === 'SYSTEM' && cardLabels[i].value.valueUser === currentUserId) {
                return true;
            }
        }

        return false;
    };

    // FIXME: useless parameters, check one by one
    services.factory('Card', function ($http, $window, FileUploader) {
        return {
            findCardByBoardShortNameAndSeqNr: function (shortName, seqNr) {
                return $http.get('api/card-by-seq/' + shortName + '-' + seqNr).then(extractData);
            },
            findCardById: function (id) {
                return $http.get('api/card/' + id).then(extractData);
            },
            findCardsByMilestone: function (projectName) {
                function insertStatusIfExists(milestone, source, target, status) {
                    if (source[status] !== undefined) {
                        target[target.length] = {status: status, count: source[status]};
                        milestone.totalCards += source[status];
                    }
                }

                function orderByStatus(milestone) {
                    milestone.totalCards = 0;
                    var sorted = [];

                    insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, 'BACKLOG');
                    insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, 'OPEN');
                    insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, 'DEFERRED');
                    insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, 'CLOSED');

                    return sorted;
                }

                return $http.get('api/project/' + projectName + '/cards-by-milestone').then(extractData).then(function (response) {
                    response.cardsCountByStatus = {};

                    angular.forEach(response.milestones, function (milestone) {
                        response.cardsCountByStatus[milestone.labelListValue.id] = orderByStatus(milestone);
                    });

                    return response;
                });
            },
            findCardsByMilestoneDetail: function (projectName, milestoneId) {
                return $http.get('api/project/' + projectName + '/cards-by-milestone-detail/' + milestoneId).then(extractData);
            },
            findByColumn: function (columnId) {
                return $http.get('api/column/' + columnId + '/card').then(extractData);
            },
            moveAllFromColumnToLocation: function (columnId, cardIds, location) {
                return $http.post('api/card/from-column/' + columnId + '/to-location/' + location, {cardIds: cardIds}).then(extractData);
            },
            update: function (id, name) {
                return $http.post('api/card/' + id, {name: name}).then(extractData);
            },
            description: function (id) {
                return $http.get('api/card/' + id + '/description').then(extractData);
            },
            clone: function (cardId, columnId) {
                return $http.post('api/card/' + cardId + '/clone-to/column/' + columnId).then(extractData);
            },

            updateDescription: function (id, description) {
                return $http.post('api/card/' + id + '/description', {content: description}).then(extractData);
            },
            comments: function (id) {
                return $http.get('api/card/' + id + '/comments').then(extractData);
            },
            addComment: function (id, comment) {
                return $http.post('api/card/' + id + '/comment', comment).then(extractData);
            },

            updateComment: function (commentId, comment) {
                return $http.post('api/card-data/comment/' + commentId, comment).then(extractData);
            },

            deleteComment: function (commentId) {
                return $http['delete']('api/card-data/comment/' + commentId).then(extractData);
            },
            undoDeleteComment: function (eventId) {
                return $http.post('api/card-data/undo/' + eventId + '/comment', null).then(extractData);
            },
            //

            actionLists: function (id) {
                return $http.get('api/card/' + id + '/actionlists').then(extractActionLists);
            },

            addActionList: function (id, actionList) {
                return $http.post('api/card/' + id + '/actionlist', {content: actionList}).then(extractData);
            },

            updateActionList: function (itemId, content) {
                return $http.post('api/card-data/actionlist/' + itemId + '/update', {content: content}).then(extractData);
            },

            deleteActionList: function (itemId) {
                return $http['delete']('api/card-data/actionlist/' + itemId).then(extractData);
            },

            undoDeleteActionList: function (eventId) {
                return $http.post('api/card-data/undo/' + eventId + '/actionlist', null).then(extractData);
            },

            addActionItem: function (referenceId, actionItem) {
                return $http.post('api/card-data/actionlist/' + referenceId + '/item', {content: actionItem}).then(extractData);
            },
            toggleActionItem: function (itemId, status) {
                return $http.post('api/card-data/actionitem/' + itemId + '/toggle/' + status, null).then(extractData);
            },
            updateActionItem: function (itemId, content) {
                return $http.post('api/card-data/actionitem/' + itemId + '/update', {content: content}).then(extractData);
            },
            deleteActionItem: function (itemId) {
                return $http['delete']('api/card-data/actionitem/' + itemId).then(extractData);
            },
            undoDeleteActionItem: function (eventId) {
                return $http.post('api/card-data/undo/' + eventId + '/actionitem', null).then(extractData);
            },
            updateActionItemOrder: function (listId, order) {
                return $http.post('api/card-data/actionlist/' + listId + '/order', order).then(extractData);
            },
            updateActionListOrder: function (cardId, order) {
                return $http.post('api/card/' + cardId + '/order/actionlist', order).then(extractData);
            },
            moveActionItem: function (itemId, newActionList, sortedActionLists) {
                return $http.post('api/card-data/actionitem/' + itemId + '/move-to-actionlist/' + newActionList, sortedActionLists).then(extractData);
            },
            files: function (id) {
                return $http.get('api/card/' + id + '/files').then(extractData);
            },
            getMaxFileSize: function () {
                return $http.get('api/configuration/max-upload-file-size').then(extractData);
            },
            getFileUploader: function (cardId) {
                return new FileUploader({
                    url: 'api/card/' + cardId + '/file',
                    alias: 'files',
                    autoUpload: true,
                    headers: { 'x-csrf-token': $window.csrfToken }
                });
            },
            getNewCardFileUploader: function () {
                return new FileUploader({
                    url: 'api/card/file',
                    alias: 'files',
                    autoUpload: true,
                    headers: { 'x-csrf-token': $window.csrfToken }
                });
            },
            deleteFile: function (cardDataId) {
                return $http['delete']('api/card-data/file/' + cardDataId, null).then(extractData);
            },

            undoDeleteFile: function (eventId) {
                return $http.post('api/card-data/undo/' + eventId + '/file').then(extractData);
            },
            activity: function (cardId) {
                return $http.get('api/card/' + cardId + '/activity').then(extractData);
            },

            activityData: function (id) {
                return $http.get('api/card-data/activity/' + id).then(extractData);
            },

            isWatchedByUser: function (labels, userId) {
                return isInCardLabels(labels, 'WATCHED_BY', userId);
            },

            isAssignedToUser: function (labels, userId) {
                return isInCardLabels(labels, 'ASSIGNED', userId);
            }
        };
    });
}());
