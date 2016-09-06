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
        angular.forEach(metadata.labels, function(label, labelId) {
            if(label.name === 'MILESTONE') {
                angular.forEach(metadata.labelListValues, function(labelValue, labelValueId) {
                    if(labelValue.cardLabelId == label.id) {
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
            if(label.domain === 'USER') {
                metadata.userLabels.push(label);
            }
        });

	    return metadata;
	};

	services.factory('Project', function ($http, $filter, StompClient) {
		return {

			//ordered by archived, name
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
				return $http.post('api/project/' + project.shortName, {name: project.name, description: project.description, archived: project.archived}).then(extractData);
			},

			suggestShortName: function (name) {
				return $http.get('api/suggest-project-short-name', {params: {name: name}}).then(extractData);
			},

			checkShortName: function(name) {
				return $http.get('api/check-project-short-name', {params: {name: name.toUpperCase()}}).then(extractData).then(function(res) {
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

			getMetadata: function(shortName) {
				return $http.get('api/project/' + shortName + '/metadata').then(extractMetadata);
			},

			getAvailableTrelloBoards: function (trello) {
				return $http.post('/api/import/trello/boards', trello).then(extractData);
			},

			importFromTrello: function (trello) {
				return $http.post('api/import/trello/', trello).then(extractData);
			},

			updateColumnDefinition: function (shortName, definition, color) {
				return $http.put('api/project/' + shortName + '/definition', {definition: definition, color: color}).then(extractData);
			},

            findAllColumns: function (shortName) {
                return $http.get('api/project/' + shortName + '/columns-in/').then(extractData);
            },

            loadMetadataAndSubscribe: function(shortName, targetObject, assignToMap) {

            	var Project = this;

            	function loadProjectMetadata() {
                	Project.getMetadata(shortName).then(function(metadata) {
                		if(assignToMap) {
                			targetObject[shortName] = metadata;
                		} else {
                			targetObject.metadata = metadata;
                		}
                    });
                }

                loadProjectMetadata();

                return StompClient.subscribe('/event/project/' + shortName, function(ev) {
                	if(ev.body === '"PROJECT_METADATA_HAS_CHANGED"') {
                		loadProjectMetadata();
                	}
                });
            }
		};
	});


})();
