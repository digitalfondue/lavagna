(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	services.factory('UsersAdministration', function ($http) {

		return {
			toggle: function (userId, enabled) {
				return $http.post('api/user/' + userId + '/enable', {enabled: enabled});
			},

			addUser: function (userToAdd) {
				userToAdd.username = userToAdd.username.trim();
				return $http.post('api/user/insert', userToAdd);
			}
		};
	});
})();