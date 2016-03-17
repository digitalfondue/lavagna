(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	var extractData = function (data) {
		return data.data;
	};

	services.factory('Project', function ($http, $filter) {
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
				return $http.get('api/check-project-short-name', {params: {name: name.toUpperCase()}}).then(extractData).then(function(res) {return res;});
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
            }
		};
	});


})();
