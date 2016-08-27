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
        controller:function(BulkOperationModal, Project, $mdDialog) {
        	var ctrl = this;

        	ctrl.bulkOperationModal = BulkOperationModal;
        	ctrl.sideBarLocation = null;
        	ctrl.toggledSidebar = false;

        	ctrl.toggleSidebar = function(location) {
        	    if(location === ctrl.sideBarLocation) {
        	        ctrl.toggledSidebar = !ctrl.toggledSidebar;
        	    } else {
        	        ctrl.sideBarLocation = location;
                    ctrl.toggledSidebar = true;
        	    }
        	};

        	ctrl.newColumn = function() {
        		$mdDialog.show({
					template: '<lvg-dialog-new-column columns-definition="vm.columnsDefinition" project-name="vm.projectShortName" board-name="vm.boardShortName"></lvg-dialog-new-column>',
					locals: {
                        boardShortName: ctrl.board.shortName
                    },
                    bindToController: true,
                    resolve: {
                        definitions: function() {
                            return Project.columnsDefinition(ctrl.project.shortName);
                        }
                    },
                    controller: function(definitions) {
                        this.columnsDefinition = definitions;
                    },
                    controllerAs: 'vm'
				});
        	};
        }
    });
})();
