(function () {
    var components = angular.module('lavagna.components');

    components.component('lvgProjectMilestone', {
        bindings: {
            project: '<',
            id: '<'
        },
        require: {projectMilestones: '^^lvgProjectMilestones'},
        templateUrl: 'app/components/project/milestone/project-milestone.html',
        controller: ['$rootScope', 'Card', 'BoardCache', ProjectMilestoneController]
    });

    function ProjectMilestoneController($rootScope, Card, BoardCache) {
        var ctrl = this;

        function loadColumn(card) {
            BoardCache.column(card.columnId).then(function (col) {
                card.column = col;
            });
        }

        ctrl.loadColumn = loadColumn;

        ctrl.reloadMilestone = function () {
            ctrl.statusColors = ctrl.projectMilestones.statusColors;

            var idToCheck = parseInt(ctrl.id);

            for (var i = 0; i < ctrl.projectMilestones.cardsByMilestone.length; i++) {
                var milestone = ctrl.projectMilestones.cardsByMilestone[i];

                if ((!isNaN(idToCheck) && milestone.labelListValue.id === idToCheck)) {
                    ctrl.milestone = milestone;
                    break;
                }
            }

            if (ctrl.milestone) {
                ctrl.cardsCountByStatus = ctrl.projectMilestones.cardsCountByStatus[ctrl.milestone.labelListValue.id];

                Card.findCardsByMilestoneDetail(ctrl.project.shortName, ctrl.milestone.labelListValue.id).then(function (detail) {
                    ctrl.detail = detail;
                });
            }
        };

        ctrl.$onInit = function init() {
            ctrl.projectMilestones.select(parseInt(ctrl.id));
        };

        var unregEvents = $rootScope.$on('loadedMilestonesInProject', function () {
            ctrl.reloadMilestone();
        });

        ctrl.$onDestroy = function destroy() {
            ctrl.projectMilestones.unselect();
            unregEvents();
        };
    }
}());
