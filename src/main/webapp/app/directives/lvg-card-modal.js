(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardModal', function () {
		return {
			restrict: 'A',
			controller: function ($scope, $element, $state) {

				$scope.goBack = function() {
					//go to parent state
					$state.go('^');
				}
			},
			link: function ($scope) {
				var cleanup = function () {
					$('#cardModal,#cardModalBackdrop').removeClass('in');
					$('#cardModal,#cardModalBackdrop').remove();
					$("body").removeClass('lvg-modal-open');
					$(document).unbind('keyup', $scope.escapeHandler);
				};

				$scope.close = function () {
					cleanup();
					$scope.goBack();
				};

				$scope.$on('$destroy', cleanup);

				$scope.escapeHandler = function (e) {
					if (e.keyCode == 27) {
						$scope.$apply($scope.close);
					}
				};

				$(document).bind('keyup', $scope.escapeHandler);

				$scope.clickHandler = function (event) {
					if (event.target.id == 'cardModal') {
						$scope.close();
					}
				};

				$("body").append($('<div id="cardModalBackdrop" class="lvg-modal-overlay lvg-modal-overlay-fade"></div>'));
				$("body").addClass('lvg-modal-open');
				$("#cardModal,#cardModalBackdrop").addClass('in');
			}
		};
	});

})();
