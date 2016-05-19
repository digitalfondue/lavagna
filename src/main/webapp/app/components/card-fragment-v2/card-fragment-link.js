(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentLink', {
			template: '<a href="" ng-bind="::$ctrl.shortCardName"></a>',
			bindings: {
				projectName: '@',
				boardShortName: '@',
				sequenceNumber: '@',
				targetState: '@',
				dynamicLink: '@'
			},
			controller: function($scope, $element, $state, $location) {
				var ctrl = this;
				ctrl.shortCardName = ctrl.boardShortName + ' - ' + ctrl.sequenceNumber;
				
				ctrl.$postLink = function() {
					var $a = $element.find("a");
					
					function updateUrl(q, page) {
						return $state.href(ctrl.targetState, {
							projectName: ctrl.projectName, 
							shortName: ctrl.boardShortName, 
							seqNr: ctrl.sequenceNumber, 
							q:  q, 
							page: page});
					}
					
					$a.attr('href', updateUrl($location.search().q, $location.search().page));
					
					if(ctrl.dynamicLink === 'true') {
						$scope.$on('updatedQueryOrPage', function(ev, searchFilter) {
							$a.attr('href', updateUrl(searchFilter.location ? searchFilter.location.q : null, $location.search().page))
						});
					}
				}
			}
		});
	
})();