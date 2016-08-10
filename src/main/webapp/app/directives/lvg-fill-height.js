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
	directives.directive('lvgFillHeight', function($window, $timeout) {
		return {
			restrict : 'A',
			link: function($scope, element, attrs) {
				
				var domElement = element[0];

				var resizeHandler = function() {
					var toggle = "true" === attrs.lvgFillHeight;
					var margin = parseInt(attrs.lvgFillHeightMargin, 10) || 250;
					//sidebar related
					//-> the sidebar has a width of 250px
					var wHeight = $window.innerHeight;//
					var wWidth = $window.innerWidth;//
					
					domElement.style.width = (wWidth - (toggle ? margin : 0)) + 'px'; 
					
					var elemOffsetTop = domElement.offsetTop;
					
					var maxHeight = wHeight - elemOffsetTop;

					domElement.style.height = (wHeight - elemOffsetTop) + 'px' 

					$timeout(function() {$scope.maxHeight = maxHeight;},0);
				};
				resizeHandler();

				//sidebar related
				attrs.$observe('lvgFillHeight', resizeHandler);
				
				$window.addEventListener('resize', resizeHandler);

				$scope.$watch(function() {
						return domElement.offsetTop;
					}, function() {
						resizeHandler();
					}
				);

				$scope.$on('$destroy', function() {
					$window.removeEventListener('resize', resizeHandler);
				});
			}
		};
	});

	directives.directive('lvgUpdateColumnSize', function($window, $timeout) {
		return {
			restrict : 'A',
			link : function($scope, element, attrs) {
				
				var domElement = element[0];
				
				$scope.$watch(function() {
				        var head = domElement.querySelector('.panel-heading');
				        var panelHeadHeight = head ? head.offsetHeight : 0;

				        var footer = domElement.querySelector('.panel-footer');
                        var panelFooterHeight = footer ? footer.offsetHeight : 0;

				        return panelHeadHeight + panelFooterHeight;
				    }, function(v) {
					    $scope.panelHeadAndFooterSize = v;
				});
			}
		};
	});

	directives.directive('lvgFillSidebarHeight', function($window) {
		return {
			restrict : 'A',
			link: function($scope, element, attrs) {
				var domElement = element[0];

				var resizeHandler = function() {
					var wHeight = $window.innerHeight;//
					domElement.style.height = (wHeight - 102)+'px'; //fixed
				};
				resizeHandler();
				
				
				$window.addEventListener('resize', resizeHandler);

				$scope.$on('$destroy', function() {
					$window.removeEventListener('resize', resizeHandler);
				});

				//sidebar related
				attrs.$observe('lvgFillSidebarHeight', resizeHandler);
			}
		};
	});

	directives.directive('lvgFillSidebarCardsHeight', function($window) {
		return {
			restrict : 'A',
			link: function($scope, element, attrs) {
				
				var domElement = element[0];

				var resizeHandler = function() {
					var parentHeight = $window.innerHeight - 120;//fixed
					var offset = domElement.offsetTop;

					var hasMoreCards = attrs.lvgFillSidebarCardsHeightHasMore === "true";
					var currentPage = parseInt(attrs.lvgFillSidebarCardsHeightCurrentPage, 0) || 0;

					if(hasMoreCards || currentPage) {
						offset = offset + 36;
					}
					domElement.style.height =  (parentHeight - offset - 14) + 'px'; //14: based on margin, padding
				};
				resizeHandler();
				$window.addEventListener('resize', resizeHandler);

				$scope.$on('$destroy', function() {
					$window.removeEventListener('resize', resizeHandler);
				});

				//sidebar related
				attrs.$observe('lvgFillSidebarCardsHeight', resizeHandler);
				attrs.$observe('lvgFillSidebarCardsHeightHasMore', resizeHandler);
				attrs.$observe('lvgFillSidebarCardsHeightCurrentPage', resizeHandler);
			}
		};
	});
})();
