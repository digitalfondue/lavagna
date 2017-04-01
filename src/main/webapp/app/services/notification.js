(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    services.factory('Notification', function ($rootScope, $timeout) {
        var notifications = [];

        $rootScope.$on('$stateChangeSuccess', function () {
            notifications.length = 0; // empty
        });

        return {
            addNotification: function (_type, _data, _dismissable, _undo, _undoFn) {
                notifications.push({event: _data, type: _type, dismissable: _dismissable, undo: _undo, undoFn: _undoFn, acknowledge: function () {
                    var index = notifications.indexOf(this);

                    notifications.splice(index, 1);
                }
                });
            },
            addAutoAckNotification: function (_type, _data, _undo) {
                var notification = {event: _data, type: _type, undo: _undo, acknowledge: function () {
                    var index = notifications.indexOf(this);

                    notifications.splice(index, 1);
                }
                };

                notifications.push(notification);
                $timeout(function () {
                    var index = notifications.indexOf(notification);

                    notifications.splice(index, 1);
                }, 5000);
            },
            acknowledgeNotification: function (notification) {
                var index = notifications.indexOf(notification);

                notifications.splice(index, 1);
            },
            getNotifications: function () {
                return notifications;
            }
        };
    });
}());
