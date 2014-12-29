(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSidebar', ['$rootScope', '$state', '$window', '$http', 'User', 'StompClient', 
	                                    function ($rootScope, $state, $window, $http, User, StompClient) {
		
		
		
		return {
			restrict: 'E',
			templateUrl: 'partials/fragments/lavagna-sidebar.html',
			link: function ($scope, $element, $attr) {
				
				
				var currentlyFetching = {then: function (f) {
					f();
				}};

				var v = setInterval(function () {
					currentlyFetching = $http.get('api/keep-alive');
				}, 15 * 1000);
				
				$scope.$on('$destroy', function () {
					clearInterval(v);
				});
				
				$scope.logout = function () {
					clearInterval(v);
					currentlyFetching.then(function () {
						User.current().then(function (u) {
							StompClient.disconnect(function () {
								$http.post('logout' + '/' + u.provider + '/').then(function (res) {
									if (u.provider === 'persona' && res.data && res.data.redirectToSelf) {
										$window.location.href = $("base").attr('href') + 'logout/persona';
									} else {
										$window.location.reload();
									}
								});
							});
						});
					});
				};
				
				var escapeHandler = function (e) {
					if (e.keyCode == 27) {
						$scope.$apply(function() {
							$scope.sidebarOpen = false;
						});
					}
				};

				var clickHandler = function (event) {
					$scope.$apply(function() {
						$scope.sidebarOpen = false;
					});
				};
				
				var openSidebar = function() {
					$("body").append($('<div id="sidebarBackdrop" class="lvg-modal-overlay lvg-modal-overlay-fade"></div>'));
					$("body").addClass('lvg-modal-open');
					$("#sidebarBackdrop").addClass('in');
					$(document).bind('keyup', escapeHandler);
					$('#sidebarBackdrop').bind('click', clickHandler);
				};
				
				var closeSidebar = function() {
					$('#sidebarBackdrop').unbind('click', clickHandler);
					$('#sidebarBackdrop').removeClass('in');
					$('#sidebarBackdrop').remove();
					$("body").removeClass('lvg-modal-open');
					$(document).unbind('keyup', escapeHandler);
				}
				
				$scope.sidebarState = $state.current.name;

				$rootScope.$on('$stateChangeSuccess',
					function (event, toState, toParams, fromState, fromParams) {
						$scope.sidebarOpen = false;
						$scope.sidebarState = $state.current.name;
					}
				);
				
				$scope.$watch('sidebarOpen',function(status) {
					status ? openSidebar() : closeSidebar();
				});
				
				$("[data-toggle='sidebar']").click(function() {
					$scope.$apply(function() {
						$scope.sidebarOpen = true;
					});
				});
				
			}
		}
	}]);
})();