(function () {


    'use strict';
    var services = angular.module('lavagna.services');
    services.factory('Integrations', ['$http', Integrations]);

    function extractData(data) {
		return data.data;
	}

    function Integrations($http) {
        return {
            getAll: function() {
                return $http.get('/api/plugin').then(extractData);
            },
            remove: function(name) {
                return $http['delete']('/api/plugin/'+name).then(extractData);
            }
        }
    }



})()
