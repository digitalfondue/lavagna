(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	services.factory('StompClient', function ($q, $log, $rootScope, $window, CONTEXT_PATH, User, Notification) {

		var defer = $q.defer();
		
		var callbacks = {
				
		};

		defer.promise.disconnect = function (f) {
			return this.then(function (v) {
				$log.log('stomp client disconnect');
				return v.disconnect(f);
			});
		};

		defer.promise.subscribe = function (scope, path, callback, headers) {

			this.then(function (v) {
				
				if(!callbacks[path] || callbacks[path].length == 0) {
					callbacks[path] = [];
					
					$log.log('stomp client subscribe at', path);
					
					callbacks[path].subscription = v.subscribe(path, function (msg) {
						scope.$apply(function() {
							angular.forEach(callbacks[path], function(cb) {
								try {
									cb(msg);
								} catch(e) {
								}
							});
						});
					}, headers);
				}
				
				
				$log.log('callback registered for', path);
				callbacks[path].push(callback);
				
				
				scope.$on('$destroy', function () {
					$log.log('callback unregistered for', path);
					callbacks[path].splice(callbacks[path].indexOf(callback), 1);
					if(callbacks[path].length == 0) {
						$log.log('stomp client unsubscribe from', path);
						callbacks[path].subscription.unsubscribe();
					}
				});
				
			});
		};

		var ignoreErrorOnApplicationDestroy = false;

		$window.addEventListener("beforeunload",function() {ignoreErrorOnApplicationDestroy = true;});

		User.isAuthenticated().then(function userIsAuth() {

			var socket = new SockJS(CONTEXT_PATH + 'api/socket');
			var stompClient = Stomp.over(socket);

			stompClient.connect('', '', function (frame) {
				$log.log('stomp client connect', frame, socket.protocol);
				defer.resolve(stompClient);
			}, function (error) {
				$log.log('stomp client error', error);
				if(!ignoreErrorOnApplicationDestroy) {
					$rootScope.$apply(function () {
						Notification.addNotification('error', {key: 'notification.error.connectionFailure'}, false, false);
					});
				}
			});
		}, function anonymousUser() {

		});

		return defer.promise;
	});
})();
