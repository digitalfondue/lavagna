(function () {

	'use strict';

	

	angular.module('lavagna.components').component('lvgSearchControls', {
		bindings: {
			selectAllInPage: '=',
			deselectAllInPage: '=',
			selectedCardsCount: '=',
			inProject: '=',
			collectIdsByProject: '=',
			triggerSearch: '='
		},
		templateUrl: 'app/components/search-controls/search-controls.html'
	});
})();