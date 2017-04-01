(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    services.factory('StompClient', function ($q, $log, $rootScope, $window, CONTEXT_PATH, User, Notification) {
        var cnt = 0;

        var defer = $q.defer();

        var callbacks = {

        };

        defer.promise.disconnect = function (f) {
            return this.then(function (v) {
                $log.log('stomp client disconnect');

                return v.disconnect(f);
            });
        };

        defer.promise.subscribe = function (path, callback) {
            var onDestroyPromise = this.then(function (v) {
                var identifier = '__id__' + (cnt++);

                if (!callbacks[path] || callbacks[path].count === 0) {
                    callbacks[path] = {count: 0};

                    $log.log('stomp client subscribe at', path);

                    callbacks[path].subscription = v.subscribe(path, function (msg) {
                        angular.forEach(callbacks[path], function (cb, key) {
                            if (key.indexOf('__id__') === 0) {
                                $log.log('calling callback for path ' + path + ' with key ' + key);
                                $rootScope.$applyAsync(function () { cb.callback(msg); });
                            }
                        });
                    });
                }

                $log.log('callback with id ' + identifier + ' registered for', path);
                callbacks[path][identifier] = {callback: callback};
                callbacks[path].count++;

                function destroyCallback() {
                    $log.log('callback with id ' + identifier + ' unregistered for', path);
                    if (!callbacks[path][identifier]) {
                        $log.log('callback with id ' + identifier + ' for ' + path + ' has already been unregistered!');

                        return;
                    }
                    delete callbacks[path][identifier];
                    callbacks[path].count--;
                    $log.log('count for path ' + path, callbacks[path].count);
                    if (callbacks[path].count === 0) {
                        $log.log('stomp client unsubscribe from', path);
                        callbacks[path].subscription.unsubscribe();
                    }
                }

                return destroyCallback;
            });

            return function () {
                onDestroyPromise.then(function (c) {
                    c();
                });
            };
        };

        var ignoreErrorOnApplicationDestroy = false;

        $window.addEventListener('beforeunload', function () { ignoreErrorOnApplicationDestroy = true; });

        User.isAuthenticated().then(function userIsAuth() {
            var socket = new SockJS(CONTEXT_PATH + 'api/socket');
            var stompClient = Stomp.over(socket);

            stompClient.connect('', '', function (frame) {
                $log.log('stomp client connect', frame, socket.protocol);
                defer.resolve(stompClient);
            }, function (error) {
                $log.log('stomp client error', error);
                if (!ignoreErrorOnApplicationDestroy) {
                    $rootScope.$applyAsync(function () {
                        Notification.addNotification('error', {key: 'notification.error.connectionFailure'}, false, false);
                    });
                }
            });
        }, function anonymousUser() {

        });

        return defer.promise;
    });
}());
