(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardModal', function () {
		return {
			restrict: 'A',
			scope: {
			    onClose: '='
			},
			controller: function ($scope, $element, $state) {

				$scope.goBack = function() {
					//go to parent state
					$scope.onClose();
					$state.go('^');
				}
			},
			link: function ($scope) {
				
				var close = function () {
					cleanup();
					$scope.goBack();
				};
				
				var escapeHandler = function (e) {
					if (e.keyCode == 27) {
						$scope.$apply(close);
					}
				};
				
				var cleanup = function () {
					$('#cardModal,#cardModalBackdrop').removeClass('in');
					$('#cardModal,#cardModalBackdrop').remove();
					$("body").removeClass('lvg-modal-open');
					$(document).unbind('keyup', escapeHandler);
				};

				$scope.$on('$destroy', cleanup);
				$(document).bind('keyup', escapeHandler);

				$("body").append($('<div id="cardModalBackdrop" class="lvg-modal-overlay lvg-modal-overlay-fade"></div>'));
				$("body").addClass('lvg-modal-open');
				$("#cardModal,#cardModalBackdrop").addClass('in');
			}
		};
	});

})();
