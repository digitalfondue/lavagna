(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgNavbar', ['$rootScope', '$state', function ($rootScope, $state) {
		return {
			restrict: 'E',
			templateUrl: 'partials/fragments/lvg-navbar.html',
			transclude: true,
			link : function($scope, $element, $attrs) {
				$scope.navigationState = $state.current.name;
				
				$scope.navbarType = $attrs.navbarType;
				
				$rootScope.$on('$stateChangeSuccess',
					function (event, toState, toParams, fromState, fromParams) {
						$scope.navigationState = $state.current.name;
					}
				);
				
				$scope.stateBelongsTo = function(parentState) {
					return $scope.navigationState.indexOf(parentState) == 0;
				}
			}
		}
	}]);
})();