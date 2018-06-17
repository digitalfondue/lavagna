(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data;
    };

    services.factory('User', function ($http, $q, $stateParams, $window, $timeout) {
        var cached = null;

        var checkPermission = function (currentUser, permissionToCheck, projectName) {
            return (currentUser.basePermissions[permissionToCheck] !== undefined || (projectName !== undefined &&
            currentUser.permissionsForProject[projectName] !== undefined &&
            currentUser.permissionsForProject[projectName][permissionToCheck] !== undefined));
        };

        var pendingUserRequests = null;
        var usersToRequestMap = {};

        return {
            checkPermissionInstant: checkPermission,

            loginUrl: function () {
                var baseHref = $window.document.querySelector('base').attributes.href.value;
                var reqUrlWithoutContextPath = $window.location.pathname.substr(baseHref.length - 1);

                return 'login?reqUrl=' + encodeURIComponent(reqUrlWithoutContextPath);
            },

            current: function () {
                var u = $http.get('api/self').then(extractData);

                cached = u;

                return u;
            },

            currentCachedUser: function () {
                if (cached === null) {
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
                return $http['delete']('api/calendar/token').then(extractData);
            },
            getCalendar: function () {
                return $http.get('api/calendar/user').then(extractData);
            },
            getProjectCalendar: function (projectShortName) {
                return $http.get('api/calendar/project/' + projectShortName).then(extractData);
            },
            updateProfile: function (profile) {
                return $http.post('api/self', profile);
            },
            updateMetadata: function (metadata) {
                return $http.post('api/self/metadata', metadata);
            },
            changePassword: function (currentPassword, newPassword) {
                return $http.post('api/self/password', {currentPassword: currentPassword, newPassword: newPassword});
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

            formatName: function (user) {
                if (user.displayName) { return user.displayName + ' (' + user.provider + ':' + user.username + ')'; }

                return user.provider + ':' + user.username;
            },

            list: function (projectShortName) {
                if (projectShortName !== undefined) {
                    return $http.get('api/project/' + projectShortName + '/user/list').then(extractData);
                } else {
                    return $http.get('api/user/list').then(extractData);
                }
            },

            user: function (userId) {
                // try to accumulate enough requests to do a bulk one, this avoid a storm of little requests

                if (pendingUserRequests) {
                    // remove pending requests
                    $timeout.cancel(pendingUserRequests);
                    pendingUserRequests = null;
                }

                if (usersToRequestMap[userId]) {
                    return usersToRequestMap[userId].promise;
                }

                usersToRequestMap[userId] = $q.defer();
                var deferred = usersToRequestMap[userId];

                pendingUserRequests = $timeout(function () {
                    pendingUserRequests = null;
                    var reqMap = angular.copy(usersToRequestMap);

                    usersToRequestMap = {};

                    var ids = [];

                    angular.forEach(reqMap, function (v, uid) {
                        ids.push(uid);
                    });

                    $http.get('api/user/bulk', {params: {ids: ids}}).then(function (res) {
                        angular.forEach(reqMap, function (v, uid) {
                            reqMap[uid].resolve(res.data[uid]);
                        });
                    });
                }, 50);

                return deferred.promise;
            },

            getUserProfile: function (provider, username, page) {
                return $http.get('api/user/profile/' + provider + '/' + username, {params: {page: page}}).then(extractData);
            },

            getUserActivity: function (provider, username) {
                return $http.get('api/user/activity/' + provider + '/' + username).then(extractData);
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

            hasPermission: function (permissionToCheck, projectName, useCurrentProject) {
                if(useCurrentProject === true) {
                    projectName = $stateParams.projectName;
                }

                return this.hasAllPermissions([permissionToCheck], projectName);
            },

            hasPermissions: function (permissionsToCheck, projectName) {
                return this.currentCachedUser().then(function (currentUser) {
                    var deferred = $q.defer();
                    var permissions = {};

                    for (var i = 0; i < permissionsToCheck.length; i++) {
                        permissions[permissionsToCheck[i].trim()] =
                            checkPermission(currentUser, permissionsToCheck[i].trim(), projectName);
                    }

                    deferred.resolve(permissions);

                    return deferred.promise;
                });
            },

            hasAtLeastOnePermission: function (permissionsToCheck, projectName) {
                return this.currentCachedUser().then(function (currentUser) {
                    var deferred = $q.defer();

                    for (var i = 0; i < permissionsToCheck.length; i++) {
                        if (checkPermission(currentUser, permissionsToCheck[i].trim(), projectName)) {
                            deferred.resolve(true);

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
                        deferred.resolve(true);
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
}());
