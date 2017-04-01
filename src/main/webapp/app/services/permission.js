(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data;
    };

    services.factory('Permission', function ($http) {
        // TODO: cleanup, it's ugly :D
        function permissionService(projectName) {
            var project = function () {
                return projectName !== undefined ? ('project/' + projectName + '/') : '';
            };

            return {
                findAllRolesAndRelatedPermissions: function () {
                    return $http.get('api/' + project() + 'role').then(extractData);
                },

                createRole: function (roleName) {
                    return $http.post('api/' + project() + 'role', {name: roleName}).then(extractData);
                },

                deleteRole: function (roleName) {
                    return $http['delete']('api/' + project() + 'role/' + roleName);
                },

                updateRole: function (roleName, permissions) {
                    return $http.post('api/' + project() + 'role/' + roleName, {permissions: permissions});
                },

                allAvailablePermissionsByCategory: function () {
                    return $http.get('api/' + project() + 'role/available-permissions').then(extractData);
                },

                findUsersWithRole: function (roleName) {
                    return $http.get('api/' + project() + 'role/' + roleName + '/users/').then(extractData);
                },

                addUserToRole: function (userId, roleName) {
                    return $http.post('api/' + project() + 'role/' + roleName + '/users/', {userIds: [userId]}).then(extractData);
                },

                removeUserToRole: function (userId, roleName) {
                    return $http.post('api/' + project() + 'role/' + roleName + '/remove/', {userIds: [userId]}).then(extractData);
                },

                findUserRoles: function (userId) {
                    return $http.get('api/user-roles/' + userId + '/').then(extractData);
                }
            };
        }

        var s = permissionService();

        s.forProject = permissionService;

        s.toggleSearchPermissionForAnonymousUsers = function (value) {
            return $http.post('api/role/ANONYMOUS/toggle-search-permission', {value: value});
        };

        return s;
    });
}());
