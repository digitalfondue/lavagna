(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgBoardControls', {
        bindings: {
            board: '<',
            project: '<',
            columns: '<',
            toggledSidebar: '<',
            sideBarLocation: '<',
            unSelectAll: '&',
            selectAll: '&',
            selectedVisibleCount: '&',
            formatBulkRequest: '&',
            selectedVisibleCardsIdByColumnId: '&',
            toggleSidebar: '&'
        },
        templateUrl: 'app/components/board/controls/board-controls.html',
        controller: function (BulkOperationModal, BulkOperations, Project, $mdDialog) {
            var ctrl = this;

            ctrl.$onChanges = function (change) {
                if (change.columns && change.columns.currentValue) {
                    ctrl.hasNoColumns = change.columns.currentValue.length === 0;
                }
            };

            ctrl.bulkOperationModal = BulkOperationModal;

            var projectMetadataSubscription = Project.loadMetadataAndSubscribe(ctrl.project.shortName, ctrl.project);

            ctrl.$onDestroy = function onDestroy() {
                projectMetadataSubscription();
            };

            ctrl.hasClosedMilestones = function () {
                if (ctrl.project.metadata) {
                    for (var i = 0; i < ctrl.project.metadata.milestones.length; i++) {
                        if (ctrl.project.metadata.milestones[i].status === 'CLOSED') {
                            return true;
                        }
                    }
                }

                return false;
            };

            ctrl.setMilestone = function (milestone) {
                BulkOperations.setMilestone(ctrl.formatBulkRequest(), milestone);
            };

            ctrl.newColumn = function () {
                $mdDialog.show({
                    template: '<lvg-dialog-new-column columns-definition="vm.columnsDefinition" project-name="vm.projectShortName" board-name="vm.boardShortName"></lvg-dialog-new-column>',
                    locals: {
                        boardShortName: ctrl.board.shortName
                    },
                    bindToController: true,
                    resolve: {
                        definitions: function () {
                            return Project.columnsDefinition(ctrl.project.shortName);
                        }
                    },
                    controller: function (definitions) {
                        this.columnsDefinition = definitions;
                    },
                    controllerAs: 'vm'
                });
            };
        }
    });
}());
