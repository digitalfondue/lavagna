(function() {

	'use strict';

	var directives = angular.module('lavagna.directives');

	/*
	 *  we have the following situation:
	 *
	 *  +----------------------+     +
	 *  |  top                 |    |
	 *  +--------------------- +-------------------------- $(element).position().top
	 *  |                           +--- window.height()
	 *  |  canvas area              |
	 *  |                           |
	 *  |                           |
	 *  +----------------------     +
	 *
	 *
	 *  The canvas area must have an height that fill the current view port if we
	 *  want that the horizontal scroll bar to appear at the bottom of the page.
	 *
	 *  Additionally, we support the presence of a left sidebar with an absolute
	 *  position.
	 *
	 *
	 *  Thus:
	 */
	directives.directive('lvgUpdateColumnSize', function($window) {
		return {
			restrict : 'A',
			link : function lvgUpdateColumnSizeLink($scope, element, attrs) {

				var domElement = element[0];

				var resizeHandler = function() {
				    var wHeight = $window.innerHeight;//
                    var maxHeight = wHeight - 80; //fixed header + board-controls

				    var head = domElement.querySelector('.lvg-board-column__header');
                    var panelHeadHeight = head ? head.offsetHeight : 0;

                    domElement.querySelector('.lvg-board-column__content').style.maxHeight = (maxHeight - panelHeadHeight) + 'px';
				};
				resizeHandler();

				$window.addEventListener('resize', resizeHandler);

                $scope.$on('$destroy', function() {
                    $window.removeEventListener('resize', resizeHandler);
                });
			}
		};
	});

	directives.directive('lvgUpdateSidebarSize', function($window) {
		return {
			restrict : 'A',
			link: function lvgUpdateSidebarSizeLink($scope, element, attrs) {

				var domElement = element[0];

				var resizeHandler = function lvgFillSidebarCardsHeightResizeHandler(hasMore) {
				    var wHeight = $window.innerHeight;//
                    var maxHeight = wHeight - 80; //fixed header + board-controls

                    var head = domElement.querySelector('.lvg-board-sidebar__header');
                    var panelHeadHeight = head ? head.offsetHeight : 0;

                    var panelFooterHeight = hasMore ? 48 : 0;

                    domElement.querySelector('.lvg-board-sidebar__content').style.maxHeight = (maxHeight - panelHeadHeight - panelFooterHeight) + 'px';
				};
				resizeHandler();

				$window.addEventListener('resize', resizeHandler);

				$scope.$on('$destroy', function() {
					$window.removeEventListener('resize', resizeHandler);
				});

				//sidebar related
				attrs.$observe('lvgUpdateSidebarSizeTrigger', resizeHandler);
			}
		};
	});
})();
