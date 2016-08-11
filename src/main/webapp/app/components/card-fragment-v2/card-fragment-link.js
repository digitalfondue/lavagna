(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentLink', {
			bindings: {
				projectName: '@',
				boardShortName: '@',
				sequenceNumber: '@',
				targetState: '@',
				dynamicLink: '@'
			},
			controller: ['$scope', '$element', '$state', '$location', '$window', function lvgCardFragmentLink($scope, $element, $state, $location, $window) {
				var ctrl = this;
				const shortCardName = ctrl.boardShortName + ' - ' + ctrl.sequenceNumber;
				const isDynamicLink = ctrl.dynamicLink === 'true';
				
				ctrl.$postLink = function lvgCardFragmentLinkPostLink() {
					const a = $window.document.createElement("a");
					a.textContent = shortCardName;
					$element.append(a);
					const $a = angular.element(a);
					
					$a.attr('href', updateUrl($location.search().q, $location.search().page));
					
					if(isDynamicLink) {
						$scope.$on('updatedQueryOrPage', function(ev, searchFilter) {
							$a.attr('href', updateUrl(searchFilter.location ? searchFilter.location.q : null, $location.search().page))
						});
					}
				}
				
				
				function updateUrl(q, page) {
					return $state.href(ctrl.targetState, {
						projectName: ctrl.projectName, 
						shortName: ctrl.boardShortName, 
						seqNr: ctrl.sequenceNumber, 
						q:  q, 
						page: page});
				}
			}]
		});
	
})();