(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    services.factory('BulkOperations', function ($http, $filter, $q, Label) {
        return {
            assign: function (idsByProject, user) {
                var labelValue = Label.userVal(user.id);
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/assign', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            removeAssign: function (idsByProject, user) {
                var labelValue = Label.userVal(user.id);
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/remove-assign', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            reassign: function (idsByProject, user) {
                var labelValue = Label.userVal(user.id);
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/re-assign', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            watch: function (idsByProject, user) {
                var labelValue = Label.userVal(user.id);
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/watch', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            unWatch: function (idsByProject, user) {
                var labelValue = Label.userVal(user.id);
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/remove-watch', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            setDueDate: function (idsByProject, dueDate) {
                var labelValue = Label.dateVal($filter('extractISO8601Date')(dueDate));
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/set-due-date', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            removeDueDate: function (idsByProject) {
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/remove-due-date', {value: null, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            setMilestone: function (idsByProject, milestone) {
                var labelValue = Label.listVal(milestone.id);
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/set-milestone', {value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            removeMilestone: function (idsByProject) {
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/remove-milestone', {value: null, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            addLabel: function (idsByProject, labelToAdd, labelValue) {
                // TODO: -> in reality there can be only one project in this call...
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/add-label', {value: labelValue, labelId: labelToAdd.id, cardIds: cardIds}));
                });

                return $q.all(r);
            },
            removeLabel: function (idsByProject, labelToRemove, labelValue) {
                // TODO: -> in reality there can be only one project in this call...
                var r = [];

                angular.forEach(idsByProject, function (cardIds, projectShortName) {
                    r.push($http.post('api/project/' + projectShortName + '/bulk-op/remove-label', {labelId: labelToRemove.id, value: labelValue, cardIds: cardIds}));
                });

                return $q.all(r);
            }
        };
    });
}());
