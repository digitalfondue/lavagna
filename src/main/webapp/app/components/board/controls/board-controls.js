(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardControls', {
        bindings: {
            toggledSidebar: '=',
            sideBarLocation:'=',
            unSelectAll: '=',
            selectAll: '=',
            selectedVisibleCount: '=',
            formatBulkRequest: '=',
            selectedVisibleCardsIdByColumnId: '=',
            board:'<',
            project:'<'
        },
        templateUrl: 'app/components/board/controls/board-controls.html',
        controller:function(BulkOperationModal, $mdDialog) {
        	var ctrl = this;
        	
        	ctrl.bulkOperationModal = BulkOperationModal;
        	ctrl.sideBarLocation = 'BOARD';
        	ctrl.selectForSidebar = function() {
        		ctrl.toggledSidebar = ctrl.sideBarLocation !== 'BOARD';
        	};
        	
        	ctrl.newColumn = function() {
        		$mdDialog.show({
					template: '<lvg-dialog-new-column flex="column" layout="column" project-name="projectName" board-name="boardName"></lvg-dialog-new-column>',
					controller: function($scope) {
						$scope.projectName = ctrl.project.shortName;
						$scope.boardName = ctrl.board.shortName;
					}
				});
        	};
        }
    });
})();
