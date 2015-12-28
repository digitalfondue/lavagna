(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentAccount', {
        bindings: {},
        controller: AccountController,
        controllerAs: 'accountCtrl',
        templateUrl: 'app/components/account/account.html'
    });

    function AccountController($window, User, Notification) {
        var ctrl = this;

        User.currentCachedUser().then(function (user) {
            ctrl.userNameProfile = user.username;
            ctrl.userProvider = user.provider;
            ctrl.userUsername = user.username;
        });


        ctrl.profile = {};

        ctrl.isCurrentUser = true;

        var loadUser = function (u) {
            ctrl.user = u;
            ctrl.profile.email = u.email;
            ctrl.profile.displayName = u.displayName;
            ctrl.profile.emailNotification = u.emailNotification;
            ctrl.profile.skipOwnNotifications = u.skipOwnNotifications
        };

        ctrl.clearAllTokens = function () {
            User.clearAllTokens().then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.user.tokenCleared.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.user.tokenCleared.error'}, false);
            });
        };

        User.current().then(loadUser);


        var getOrigin = function getOrigin() {
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
            }
            return window.location.origin;
        };

        var createUrl = function (resp) {
            ctrl.calendarFeedUrl = getOrigin($window) + "/api/calendar/" + resp.token + "/calendar.ics";
            ctrl.disabledFeed = resp.disabled;
        };

        User.getCalendarToken().then(createUrl);

        ctrl.clearCalendarToken = function () {
            User.deleteCalendarToken().then(createUrl);
        };

        ctrl.updateFeed = function() {
            ctrl.disabledFeed = !ctrl.disabledFeed;
            User.updateCalendarFeedStatus(ctrl.disabledFeed).then(createUrl);
        }

        ctrl.update = function(profile) {
            User.updateProfile(profile)
                .then(User.invalidateCachedUser)
                .then(User.current).then(loadUser).then(function () {
                    Notification.addAutoAckNotification('success', {key: 'notification.user.update.success'}, false);
                }, function () {
                    Notification.addAutoAckNotification('error', {key: 'notification.user.update.error'}, false);
                });
        }
    };
})();
