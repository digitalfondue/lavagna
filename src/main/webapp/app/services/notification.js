(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	services.factory('Notification', function ($rootScope, $timeout) {
		
		$rootScope.notifications = [];
		
		$rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
			$rootScope.notifications.length = 0;//empty
		});

		return {
			addNotification: function (_type, _data, _undo, _undoFn) {
				$rootScope.notifications.push({event: _data, type: _type, undo: _undo, undoFn : _undoFn, acknowledge : function() {
						var index = $rootScope.notifications.indexOf(this);
						$rootScope.notifications.splice(index, 1);
					}
				});
			},
			addAutoAckNotification: function (_type, _data, _undo) {
				
				var notification = {event: _data, type: _type, undo: _undo, acknowledge : function() {
						var index = $rootScope.notifications.indexOf(this);
						$rootScope.notifications.splice(index, 1);
					}
				};
				
				var length = $rootScope.notifications.push(notification);
				$timeout(function () {
					var index = $rootScope.notifications.indexOf(notification);
					$rootScope.notifications.splice(index, 1);
				}, 5000);
			},
			acknowledgeNotification: function (notification) {
				var index = $rootScope.notifications.indexOf(notification);
				$rootScope.notifications.splice(index, 1);
			}
		}
	});
})();