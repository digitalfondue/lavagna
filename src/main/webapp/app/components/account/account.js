(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAccount', {
        bindings: {
        	username: '<',
        	provider: '<',
        	isCurrentUser: '<'
        },
        templateUrl: 'app/components/account/account.html',
        controller: ['$window', 'User', 'CopyToClipboard', 'Notification', 'CONTEXT_PATH', AccountController],
    });

    function AccountController($window, User, CopyToClipboard, Notification, CONTEXT_PATH) {
        var ctrl = this;

        User.currentCachedUser().then(function (user) {
        	ctrl.user = user;
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

        var createUrl = function (resp) {
            ctrl.calendarFeedUrl = CONTEXT_PATH + "api/calendar/" + resp.token + "/calendar.ics";
            ctrl.disabledFeed = resp.disabled;
        };

        User.getCalendarToken().then(createUrl);

        ctrl.clearCalendarToken = function () {
            User.deleteCalendarToken().then(createUrl);
        };

        ctrl.updateFeed = function() {
            User.updateCalendarFeedStatus(ctrl.disabledFeed).then(createUrl);
        };

        ctrl.update = function(profile) {
            User.updateProfile(profile)
                .then(User.invalidateCachedUser)
                .then(User.current).then(loadUser).then(function () {
                    Notification.addAutoAckNotification('success', {key: 'notification.user.update.success'}, false);
                }, function () {
                    Notification.addAutoAckNotification('error', {key: 'notification.user.update.error'}, false);
                });
        };

        ctrl.copyCalendarUrl = function() {
            CopyToClipboard.copy(ctrl.calendarFeedUrl).then(function() {
                Notification.addAutoAckNotification('success', {key: 'account.calendar.copy.success'}, false);
            }, function() {
                Notification.addAutoAckNotification('warning', {key: 'account.calendar.copy.failure'}, false);
            })
        };

        ctrl.changePassword = function (currentPassword, newPassword) {
            User.changePassword(currentPassword, newPassword).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.account.password.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.account.password.error'}, false);
            });
        }
    }
})();
