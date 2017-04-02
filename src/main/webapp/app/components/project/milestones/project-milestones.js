(function () {
    var components = angular.module('lavagna.components');

    components.component('lvgProjectMilestones', {
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/milestones/project-milestones.html',
        controller: ['$rootScope', 'Card', 'User', 'StompClient', 'Label', ProjectMilestonesController],
    });

    function ProjectMilestonesController($rootScope, Card, User, StompClient, Label) {
        var ctrl = this;
        //

        ctrl.showArray = showArray;
        ctrl.select = select;
        ctrl.unselect = unselect;
        ctrl.openMilestone = openMilestone;
        ctrl.closeMilestone = closeMilestone;
        //

        var unbindLabelValue = angular.noop;
        var unbindLabel = angular.noop;

        var unregStateChanges = $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState) {
            if (fromState.name === 'project.milestones.milestone.card' && toState.name === 'project.milestones.milestone') {
                loadMilestonesInProject();
            }

            if (fromState.name === 'project.milestones' && toState.name === 'project.milestones.milestone') {
                loadMilestonesInProject();
            }
        });

        ctrl.$onInit = function init() {
            ctrl.milestoneOpenStatus = {};
            loadMilestonesInProject();

            unbindLabelValue = StompClient.subscribe('/event/project/' + ctrl.project.shortName + '/label-value', loadMilestonesInProject);
            unbindLabel = StompClient.subscribe('/event/project/' + ctrl.project.shortName + '/label', loadMilestonesInProject);
        };

        ctrl.$onDestroy = function onDestroy() {
            unbindLabelValue();
            unbindLabel();
            unregStateChanges();
        };

        function showArray(array) {
            if (!array) {
                return false;
            }

            return Object.keys(array).length > 0;
        }

        function loadMilestonesInProject() {
            User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Card.findCardsByMilestone(ctrl.project.shortName);
            }).then(function (response) {
                ctrl.cardsByMilestone = response.milestones;
                ctrl.cardsCountByStatus = response.cardsCountByStatus;
                ctrl.statusColors = response.statusColors;
                $rootScope.$emit('loadedMilestonesInProject');
            });
        }

        function select(id) {
            ctrl.selectedMilestone = id;
        }

        function unselect() {
            ctrl.selectedMilestone = undefined;
        }

        function openMilestone(val) {
            Label.removeLabelListValueMetadata(val.id, 'status');
        }

        function closeMilestone(val) {
            Label.updateLabelListValueMetadata(val.id, 'status', 'CLOSED');
        }
    }
}());
