(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.directive('lvgComponentBoardSidebarCard', function () {
		return {
			templateUrl: 'app/components/board/sidelocation/card/card.html',
			restrict: 'E',
			scope: true,
			bindToController: {
				project: '=',
				board: '=',
				card: '='
			},
			controller: function(){},
			controllerAs: 'boardSidebarCardCtrl'
		}
	});
})();