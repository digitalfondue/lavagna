(function () {
    'use strict';
    var services = angular.module('lavagna.services');

    services.factory('Integrations', ['$http', Integrations]);

    function extractData(data) {
        return data.data;
    }

    function Integrations($http) {
        return {
            getAll: function () {
                return $http.get('/api/plugin').then(extractData);
            },
            remove: function (name) {
                return $http['delete']('/api/plugin/' + name).then(extractData);
            },
            update: function (name, code, properties, projects, metadata) {
                return $http.post('/api/plugin/' + name, {name: name, code: code, properties: properties, metadata: metadata, projects: projects}).then(extractData);
            },
            enable: function (name, status) {
                return $http.post('/api/plugin/' + name + '/enable/' + status).then(extractData);
            },
            create: function (integration) {
                return $http.post('/api/plugin', integration).then(extractData);
            }
        };
    }
}());
