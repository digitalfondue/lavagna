(function () {
	'use strict';
	
	angular.module('lavagna.directives').directive('lvgCardFragmentLink', function($state, $location) {
		return {
			restrict: 'E',
			template: '<a href="" ng-bind="::$ctrl.shortCardName"></a>',
			scope: {
				projectName: '@',
				boardShortName: '@',
				sequenceNumber: '@',
				targetState: '@',
				dynamicLink: '@'
			},
			link: function($scope, $element, $attrs) {
				
				var $a = $element.find("a");
				
				var ctrl = $scope.$ctrl;
				
				function updateUrl(q, page) {
					return $state.href(ctrl.targetState, {
						projectName: ctrl.projectName, 
						shortName: ctrl.boardShortName, 
						seqNr: ctrl.sequenceNumber, 
						q:  q, 
						page: page});
				}
				
				$a.attr('href', updateUrl($location.search().q, null));
				
				if(ctrl.dynamicLink === 'true') {
					$scope.$on('updatedQueryOrPage', function(ev, searchFilter) {
						$a.attr('href', updateUrl(searchFilter.location ? searchFilter.location.q : null, null))
					});
				}
			},
			bindToController: true,
			controllerAs: '$ctrl',
			controller: function($scope) {
				var ctrl = this;
				ctrl.shortCardName = ctrl.boardShortName + ' - ' + ctrl.sequenceNumber; 
			}
		};
	});
	
})();