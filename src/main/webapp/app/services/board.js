(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data;
    };

    services.factory('Board', function ($http) {
        return {
            findByShortName: function (shortName) {
                return $http.get('api/board/' + shortName).then(extractData);
            },

            suggestShortName: function (name) {
                return $http.get('api/suggest-board-short-name', {params: {name: name}}).then(extractData);
            },

            checkShortName: function (name) {
                return $http.get('api/check-board-short-name', {params: {name: name.toUpperCase()}}).then(extractData).then(function (res) {
                    return res === true;
                });
            },

            update: function (board) {
                return $http.post('api/board/' + board.shortName, {
                    name: board.name,
                    description: board.description,
                    isArchived: board.archived
                }).then(extractData);
            },

            // TODO column: move to another service

            column: function (id) {
                return $http.get('api/column/' + id).then(extractData);
            },

            moveColumnToLocation: function (id, location) {
                return $http.post('api/column/' + id + '/to-location/' + location).then(extractData);
            },

            columns: function (shortName) {
                return $http.get('api/board/' + shortName + '/columns-in').then(extractData);
            },

            columnsByLocation: function (shortName, location) {
                return $http.get('api/board/' + shortName + '/columns-in/' + location).then(extractData);
            },

            createColumn: function (shortName, createColumn) {
                return $http.post('api/board/' + shortName + '/column',
                    createColumn).then(extractData);
            },

            reorderColumn: function (shortName, location, orderedColumnId) {
                return $http.post('api/board/' + shortName + '/columns-in/' + location + '/column/order',
                    orderedColumnId).then(extractData);
            },

            // FIXME remove shortName parameter
            renameColumn: function (shortName, columnId, newName) {
                return $http.post(
                    'api/column/' + columnId + '/rename/' + newName).then(extractData);
            },

            // FIXME remove shortName parameter
            redefineColumn: function (shortName, columnId, definition) {
                return $http.post(
                    'api/column/' + columnId + '/redefine/' + definition).then(extractData);
            },

            cardsInLocationPaginated: function (shortName, location, page) {
                return $http.get('api/board/' + shortName + '/cards-in/' + location + '/' + page).then(extractData);
            },

            createCard: function (columnId, createCard) {
                return $http.post('api/column/' + columnId + '/card', createCard).then(extractData);
            },

            createCardFromTop: function (columnId, createCard) {
                return $http.post('api/column/' + columnId + '/card-top', createCard).then(extractData);
            },

            moveCardToColumn: function (cardId, previousColumnId, newColumnId, columnOrders) {
                return $http.post('api/card/' + cardId + '/from-column/' + previousColumnId + '/to-column/' + newColumnId, columnOrders)
                    .then(extractData);
            },

            moveCardToColumnEnd: function (cardId, previousColumnId, newColumnId) {
                return $http.post('api/card/' + cardId + '/from-column/' + previousColumnId + '/to-column/' + newColumnId + '/end')
                    .then(extractData);
            },

            // FIXME remove shortName parameter
            updateCardOrder: function (shortName, columnId, cardIds) {
                return $http.post('api/column/' + columnId + '/order', cardIds).then(extractData);
            },

            // FIXME remove shortName parameter
            taskStatistics: function (shortName) {
                return $http.get('api/board/' + shortName + '/task-statistics').then(extractData);
            },

            // FIXME remove shortName parameter
            statistics: function (shortName, days) {
                return $http.get('api/board/' + shortName + '/statistics/' + days).then(extractData);
            }
        };
    });
}());
