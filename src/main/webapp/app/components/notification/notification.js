(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgNotification', {
        templateUrl: 'app/components/notification/notification.html',
        controller: function (Notification) {
            this.notifications = Notification.getNotifications();
        }
    });
}());
