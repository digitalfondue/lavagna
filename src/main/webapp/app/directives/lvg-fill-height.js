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
				var $elem = $(element);

				var resizeHandler = function() {
					var toggle = "true" === attrs.lvgFillHeight;
					var margin = parseInt(attrs.lvgFillHeightMargin, 10) || 250;
					//sidebar related
					//-> the sidebar has a width of 250px
					var wHeight = $window.innerHeight;//
					var wWidth = $window.innerWidth;//
					$elem.width(wWidth - (toggle ? margin : 0));
					
					var maxHeight = wHeight - $elem.position().top;
					
					$elem.height(wHeight - $elem.position().top);
					
					$timeout(function() {$scope.maxHeight = maxHeight;},0);
					
					
				};
				resizeHandler();

				//sidebar related
				attrs.$observe('lvgFillHeight', resizeHandler);

				$($window).resize(resizeHandler);
				
				$scope.$watch(function() {
						return $(element).position().top;
					}, function() {
						resizeHandler();
					}
				);

				$scope.$on('$destroy', function() {
					$($window).unbind('resize', resizeHandler)
				});
			}
		};
	});
	
	directives.directive('lvgUpdateColumnSize', function($window, $timeout) {
		return {
			restrict : 'A',
			link : function($scope, element, attrs) {
				
				var panelHead = element.find('.panel-heading')[0];
				var panelFooter = element.find('.panel-footer')[0];
				
				$scope.$watch(function() {return panelHead.offsetHeight + panelFooter.offsetHeight;}, function(v) {
					$scope.panelHeadAndFooterSize = v;
				});
			}
		};
	});

	directives.directive('lvgFillSidebarHeight', function($window) {
		return {
			restrict : 'A',
			link: function($scope, element, attrs) {
				var $elem = $(element);

				var resizeHandler = function() {
					var wHeight = $window.innerHeight;//
					$elem.height(wHeight - 102); //fixed
				};
				resizeHandler();

				$($window).resize(resizeHandler);

				$scope.$on('$destroy', function() {
					$($window).unbind('resize', resizeHandler)
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
				var $elem = $(element);

				var resizeHandler = function() {
					var parentHeight = $window.innerHeight - 120;//fixed
					var offset = $elem.get(0).offsetTop;

					var hasMoreCards = attrs.lvgFillSidebarCardsHeightHasMore === "true";
					var currentPage = parseInt(attrs.lvgFillSidebarCardsHeightCurrentPage, 0) || 0;

					if(hasMoreCards || currentPage) {
						offset = offset + 36;
					}

					$elem.height(parentHeight - offset - 14); //14: based on margin, padding
				};
				resizeHandler();
				$($window).resize(resizeHandler);

				$scope.$on('$destroy', function() {
					$($window).unbind('resize', resizeHandler)
				});

				//sidebar related
				attrs.$observe('lvgFillSidebarCardsHeight', resizeHandler);
				attrs.$observe('lvgFillSidebarCardsHeightHasMore', resizeHandler);
				attrs.$observe('lvgFillSidebarCardsHeightCurrentPage', resizeHandler);
			}
		};
	});

	//hack
	directives.directive('lvgContainerMaxWidth', function($window) {
		return {
			restrict : 'A',
			link: function($scope, element, attrs) {
				var getBootstrapWidth = function(width, toggle, margin) {
					var subtractWidth = 0;
					if(toggle) {
						subtractWidth += margin + 60;
					}

					var trueWidth = width - subtractWidth;

					if(trueWidth >= 1200) {
						return 1140;
					}

					if(trueWidth >= 992) {
						return 940;
					}

					if(trueWidth >= 768) {
						return 720;
					}

					return trueWidth - 30;

				}

				var resizeHandler = function() {
					var toggle = parseInt(attrs.lvgContainerMaxWidth, 10);
					var margin = parseInt(attrs.lvgContainerMaxWidth, 10) || 250;

					//
					var wWidth = $window.innerWidth;//
					$(element).css('max-width', getBootstrapWidth(wWidth, toggle + 'px', margin));
				};

				resizeHandler();

				//sidebar related
				attrs.$observe('lvgContainerMaxWidth', resizeHandler);
				$($window).resize(resizeHandler);

				$scope.$on('$destroy', function() {
					$($window).unbind('resize', resizeHandler)
				});
			}
		};
	});
})();