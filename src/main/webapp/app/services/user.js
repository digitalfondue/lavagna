(function () {

    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data
    };

    services.factory('User', function ($http, $q, $stateParams) {

        var cached = null;

        var checkPermission = function (currentUser, permissionToCheck, projectName) {
            return (currentUser.basePermissions[permissionToCheck] !== undefined || (projectName !== undefined &&
            currentUser.permissionsForProject[projectName] !== undefined &&
            currentUser.permissionsForProject[projectName][permissionToCheck] !== undefined));
        };

        return {
            current: function () {
                var u = $http.get('api/self').then(extractData);
                cached = u;
                return u;
            },

            currentCachedUser: function () {
                if (cached == null) {
                    cached = this.current();
                }
                return cached;
            },

            isCurrentUser: function (provider, username) {
                return this.currentCachedUser().then(function (u) {
                    return u.provider === provider && u.username === username;
                });
            },
            updateCalendarFeedStatus: function (disabled) {
                return $http.post(' /api/calendar/disable', {isDisabled: disabled}).then(extractData);
            },
            getCalendarToken: function () {
                return $http.get('api/calendar/token').then(extractData);
            },
            deleteCalendarToken: function () {
                return $http.delete('api/calendar/token').then(extractData);
            },
            updateProfile: function (profile) {
                return $http.post('api/self', profile);
            },

            clearAllTokens: function () {
                return $http.post('api/self/clear-all-tokens').then(extractData);
            },

            invalidateCachedUser: function () {
                cached = null;
            },

            findUsersGlobally: function (searchTerm) {
                return $http.get('api/search/user', {params: {term: searchTerm}}).then(extractData);
            },

            findUsers: function (searchTerm) {
                var opts = {params: {term: searchTerm}};
                if ($stateParams.projectName) {
                    opts.params.projectName = $stateParams.projectName;
                }
                return $http.get('api/search/user', opts).then(extractData);
            },

            list: function (projectShortName) {
                if (projectShortName !== undefined) {
                    return $http.get('api/project/' + projectShortName + '/user/list').then(extractData);
                } else {
                    return $http.get('api/user/list').then(extractData);
                }
            },

            user: function (userId) {
                return $http.get('api/user/' + userId).then(extractData);
            },

            getUserProfile: function (provider, username, page) {
                return $http.get('api/user/profile/' + provider + '/' + username, {params: {page: page}}).then(extractData);
            },

            byProviderAndUsername: function (provider, username) {
                return $http.get('api/user/' + provider + '/' + username).then(extractData);
            },

            isAuthenticated: function () {
                return this.currentCachedUser().then(function (currentUser) {
                    var deferred = $q.defer();
                    if (currentUser.provider !== 'system' && currentUser.username !== 'anonymous') {
                        deferred.resolve();
                    } else {
                        deferred.reject();
                    }
                    return deferred.promise;
                });
            },

            isNotAuthenticated: function () {
                return this.currentCachedUser().then(function (currentUser) {
                    var deferred = $q.defer();
                    if (currentUser.provider === 'system' && currentUser.username === 'anonymous') {
                        deferred.resolve();
                    } else {
                        deferred.reject();
                    }
                    return deferred.promise;
                });
            },

            hasPermission: function (permissionToCheck, projectName) {
                return this.hasAllPermissions([permissionToCheck], projectName);
            },

            hasAtLeastOnePermission: function (permissionsToCheck, projectName) {
                return this.currentCachedUser().then(function (currentUser) {
                    var deferred = $q.defer();

                    for (var i = 0; i < permissionsToCheck.length; i++) {
                        if (checkPermission(currentUser, permissionsToCheck[i].trim(), projectName)) {
                            deferred.resolve();
                            return deferred.promise;
                        }
                    }

                    deferred.reject();
                    return deferred.promise;
                });
            },

            hasAllPermissions: function (permissionsToCheck, projectName) {
                return this.currentCachedUser().then(function (currentUser) {
                    var deferred = $q.defer();

                    var hasAllPermission = true;
                    for (var i = 0; i < permissionsToCheck.length; i++) {
                        hasAllPermission = hasAllPermission && checkPermission(currentUser, permissionsToCheck[i].trim(), projectName);
                    }

                    if (hasAllPermission) {
                        deferred.resolve();
                    } else {
                        deferred.reject();
                    }
                    return deferred.promise;
                });
            },

            cards: function (page) {
                return $http.get('api/self/cards/' + page).then(extractData);
            },

            cardsByProject: function (projectName, page) {
                return $http.get('api/self/project/' + projectName + '/cards/' + page).then(extractData);
            }
        };
    });
})();
