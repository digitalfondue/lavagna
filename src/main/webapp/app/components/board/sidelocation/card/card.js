(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgComponentBoardSidebarCard', {
        templateUrl: 'app/components/board/sidelocation/card/card.html',
        bindings: {
            project: '=',
            board: '=',
            card: '='
        },
        controller: function(){},
        controllerAs: 'boardSidebarCardCtrl'
    });
})();
