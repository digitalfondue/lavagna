(function () {
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

        ctrl.clearAllTokens = clearAllTokens;
        ctrl.clearCalendarToken = clearCalendarToken;
        ctrl.updateFeed = updateFeed;
        ctrl.update = update;
        ctrl.copyCalendarUrl = copyCalendarUrl;
        ctrl.changePassword = changePassword;

        ctrl.$onInit = function () {
            ctrl.profile = {};
            ctrl.isCurrentUser = true;

            User.currentCachedUser().then(function (user) {
                ctrl.user = user;
                ctrl.userNameProfile = user.username;
                ctrl.userProvider = user.provider;
                ctrl.userUsername = user.username;
            });

            User.current().then(loadUser);
            User.getCalendarToken().then(createUrl);
        };

        function loadUser(u) {
            ctrl.user = u;
            ctrl.profile.email = u.email;
            ctrl.profile.displayName = u.displayName;
            ctrl.profile.emailNotification = u.emailNotification;
            ctrl.profile.skipOwnNotifications = u.skipOwnNotifications;
        }

        function clearAllTokens() {
            User.clearAllTokens().then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.user.tokenCleared.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.user.tokenCleared.error'}, false);
            });
        }

        function createUrl(resp) {
            ctrl.calendarFeedUrl = CONTEXT_PATH + 'api/calendar/' + resp.token + '/calendar.ics';
            ctrl.disabledFeed = resp.disabled;
        }

        function clearCalendarToken() {
            User.deleteCalendarToken().then(createUrl);
        }

        function updateFeed() {
            User.updateCalendarFeedStatus(ctrl.disabledFeed).then(createUrl);
        }

        function update(profile) {
            User.invalidateCachedUser();
            User.updateProfile(profile)
                .then(User.current).then(loadUser).then(function () {
                    Notification.addAutoAckNotification('success', {key: 'notification.user.update.success'}, false);
                }, function () {
                    Notification.addAutoAckNotification('error', {key: 'notification.user.update.error'}, false);
                });
        }

        function copyCalendarUrl() {
            CopyToClipboard.copy(ctrl.calendarFeedUrl).then(function () {
                Notification.addAutoAckNotification('success', {key: 'account.calendar.copy.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('warning', {key: 'account.calendar.copy.failure'}, false);
            });
        }

        function changePassword(currentPassword, newPassword) {
            User.changePassword(currentPassword, newPassword).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.account.password.success'}, false);
            }, function () {
                Notification.addAutoAckNotification('error', {key: 'notification.account.password.error'}, false);
            });
        }
    }
}());
