(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgBoardSidebarCard', {
        templateUrl: 'app/components/board/sidebar/card/board-sidebar-card.html',
        bindings: {
            project: '=',
            board: '=',
            card: '='
        },
        controller: function(){},
        controllerAs: 'boardSidebarCardCtrl'
    });
})();
