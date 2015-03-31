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
			controller: function ($scope, $stateParams) {
				
				var refreshList = function() {
					var val = $scope.filter;
					if (val === undefined || val.trim().length == 0) {
						User.list($stateParams.projectName).then(function (res) {
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