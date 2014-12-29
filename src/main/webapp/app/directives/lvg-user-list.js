(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');
				
	directives.directive('lvgUserList', function (User) {
		return {
			restrict: 'A',
			scope: {
				userList : '=lvgUserList',
				userListLocalModel : '=lvgUserListLocalModel',
				filter: '=ngModel'
			},
			link: function ($scope, elem, attrs) {
				
				var refreshList = function() {
					var val = $scope.filter;
					if (val === undefined || val.trim().length == 0) {
						User.list().then(function (res) {
							$scope.userList = res;
						});
					} else {
						User.findUsersGlobally(val.trim()).then(function (res) {
							$scope.userList = res;
						});
					}
				}
				
				$scope.$watch('filter' , refreshList);
				
				$scope.$watch('userListLocalModel' , refreshList);
				
			}
		};
	});
})();