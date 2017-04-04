(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data;
    };

    var extractMetadata = function (data) {
        var metadata = data.data;
        // provide better format for some data

        metadata.milestones = [];
        metadata.userLabels = [];
        angular.forEach(metadata.labels, function (label, labelId) {
            if (label.name === 'MILESTONE') {
                angular.forEach(metadata.labelListValues, function (labelValue) {
                    if (labelValue.cardLabelId === label.id) {
                        metadata.milestones.push({
                            id: labelValue.id,
                            labelId: labelId,
                            name: labelValue.value,
                            status: labelValue.metadata.status || null,
                            metadata: labelValue.metadata,
                            order: labelValue.order
                        });
                    }
                });
            }
            if (label.domain === 'USER') {
                metadata.userLabels.push(label);
            }
        });

        return metadata;
    };

    services.factory('Project', function ($http, $filter, StompClient) {
        return {

            // ordered by archived, name
            list: function () {
                return $http.get('api/project').then(extractData).then(function (res) {
                    return $filter('orderBy')(res, function (elem) {
                        return (elem.archived ? '1' : '0') + elem.shortName;
                    });
                });
            },

            create: function (project) {
                return $http.post('api/project', project).then(extractData);
            },

            update: function (project) {
                return $http.post('api/project/' + project.shortName, {
                    name: project.name,
                    description: project.description,
                    isArchived: project.archived
                }).then(extractData);
            },

            suggestShortName: function (name) {
                return $http.get('api/suggest-project-short-name', {params: {name: name}}).then(extractData);
            },

            checkShortName: function (name) {
                return $http.get('api/check-project-short-name', {params: {name: name.toUpperCase()}}).then(extractData).then(function (res) {
                    return res === true;
                });
            },

            findByShortName: function (shortName) {
                return $http.get('api/project/' + shortName).then(extractData);
            },

            createBoard: function (shortName, board) {
                return $http.post('api/project/' + shortName + '/board', board).then(extractData);
            },

            findBoardsInProject: function (shortName) {
                return $http.get('api/project/' + shortName + '/board').then(extractData).then(function (res) {
                    return $filter('orderBy')(res, function (elem) {
                        return (elem.archived ? '1' : '0') + elem.shortName;
                    });
                });
            },

            columnsDefinition: function (shortName) {
                return $http.get('api/project/' + shortName + '/definitions').then(extractData);
            },

            taskStatistics: function (shortName) {
                return $http.get('api/project/' + shortName + '/task-statistics').then(extractData);
            },

            statistics: function (shortName, days) {
                return $http.get('api/project/' + shortName + '/statistics/' + days).then(extractData);
            },

            getMetadata: function (shortName) {
                return $http.get('api/project/' + shortName + '/metadata').then(extractMetadata);
            },

            getAvailableTrelloBoards: function (trello) {
                return $http.post('/api/import/trello/boards', trello).then(extractData);
            },

            importFromTrello: function (trello) {
                return $http.post('api/import/trello/', trello).then(extractData);
            },

            updateColumnDefinition: function (shortName, definition, color) {
                return $http.put('api/project/' + shortName + '/definition', {
                    definition: definition,
                    color: color
                }).then(extractData);
            },

            findAllColumns: function (shortName) {
                return $http.get('api/project/' + shortName + '/columns-in/').then(extractData);
            },

            loadMetadataAndSubscribe: function (shortName, targetObject, assignToMap) {
                var Project = this;

                function loadProjectMetadata() {
                    Project.getMetadata(shortName).then(function (metadata) {
                        metadata = extractMetadata({data: metadata});
                        if (assignToMap) {
                            targetObject[shortName] = metadata;
                        } else {
                            targetObject.metadata = metadata;
                        }
                    });
                }

                loadProjectMetadata();

                return StompClient.subscribe('/event/project/' + shortName, function (ev) {
                    if (ev.body === '"PROJECT_METADATA_HAS_CHANGED"') {
                        loadProjectMetadata();
                    }
                });
            },
            gridByDescription: function (items, skipArchived) {
                var itemsLeft = [];
                var itemsRight = [];

                var rightCount = 0;
                var leftCount = 0;

                for (var i = 0; i < items.length; i++) {
                    var item = items[i].project || items[i];

                    if (skipArchived && item.archived) {
                        continue;
                    }
                    var descriptionCount = item.description !== null ? item.description.length : 0;

                    if (descriptionCount > 0) {
                        var newLineMatch = item.description.match(/[\n\r]/g);

                        descriptionCount += newLineMatch !== null ? newLineMatch.length * 50 : 0;
                    }

                    if (leftCount <= rightCount) {
                        leftCount += descriptionCount;
                        itemsLeft.push(items[i]);
                    } else {
                        rightCount += descriptionCount;
                        itemsRight.push(items[i]);
                    }
                }

                return {
                    left: itemsLeft,
                    right: itemsRight
                };
            },
            getMailConfigs: function (shortName) {
                return $http.get('/api/project/' + shortName + '/mailConfigs').then(extractData);
            },
            createMailConfig: function (shortName, name, config, subject, body) {
                return $http.post('/api/project/' + shortName + '/mailConfig', {
                    name: name,
                    config: config,
                    subject: subject,
                    body: body
                }).then(extractData);
            },
            updateMailConfig: function (shortName, id, name, enabled, config, subject, body) {
                return $http.post('/api/project/' + shortName + '/mailConfig/' + id, {
                    name: name,
                    enabled: enabled,
                    config: config,
                    subject: subject,
                    body: body
                }).then(extractData);
            },
            deleteMailConfig: function (shortName, id) {
                return $http['delete']('/api/project/' + shortName + '/mailConfig/' + id).then(extractData);
            },
            createMailTicket: function (shortName, name, alias, sendByAlias, overrideNotification, subject, body, columnId, configId, metadata) {
                return $http.post('/api/project/' + shortName + '/ticketConfig', {
                    name: name,
                    alias: alias,
                    sendByAlias: sendByAlias,
                    overrideNotification: overrideNotification,
                    subject: subject,
                    body: body,
                    columnId: columnId,
                    configId: configId,
                    metadata: metadata
                }).then(extractData);
            },
            updateMailTicket: function (shortName, id, name, enabled, alias, sendByAlias, overrideNotification, subject, body, columnId, configId, metadata) {
                return $http.post('/api/project/' + shortName + '/ticketConfig/' + id, {
                    name: name,
                    enabled: enabled,
                    alias: alias,
                    sendByAlias: sendByAlias,
                    overrideNotification: overrideNotification,
                    subject: subject,
                    body: body,
                    columnId: columnId,
                    configId: configId,
                    metadata: metadata
                }).then(extractData);
            },
            deleteMailTicket: function (shortName, id) {
                return $http['delete']('/api/project/' + shortName + '/ticketConfig/' + id).then(extractData);
            }
        };
    });
}());
