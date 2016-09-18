(function () {

    var components = angular.module('lavagna.components');

    components.component('lvgProjectMilestones', {
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/milestones/project-milestones.html',
        controller: ['Card', 'EventBus', 'User', 'StompClient', ProjectMilestonesController],
    });

    function ProjectMilestonesController(Card, EventBus, User, StompClient) {
        var ctrl = this;
        //
        ctrl.showArray = showArray;
      	//
		
		var unbindLabelValue = angular.noop;
		var unbindLabel = angular.noop;
		var unbindMovedEvent = angular.noop;
		var unbindRenamedEvent = angular.noop;
		
		ctrl.$onInit = function init() {
			ctrl.milestoneOpenStatus = {};
			loadMilestonesInProject();
			
			unbindLabelValue = StompClient.subscribe('/event/project/' + ctrl.project.shortName + '/label-value', loadMilestonesInProject);
			unbindLabel = StompClient.subscribe('/event/project/' + ctrl.project.shortName + '/label', loadMilestonesInProject);
	        unbindMovedEvent =  EventBus.on('card.moved.event', loadMilestonesInProject);
	        unbindRenamedEvent =  EventBus.on('card.renamed.event', loadMilestonesInProject);
		};
		
		ctrl.$onDestroy = function onDestroy() {
			unbindLabelValue();
			unbindLabel();
			unbindMovedEvent();
			unbindRenamedEvent();
		};
        

        function showArray(array, minLength) {
            if (!array) {
                return false;
            }
            return Object.keys(array).length > minLength;
        }

        function loadMilestonesInProject() {
            User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Card.findCardsByMilestone(ctrl.project.shortName);
            }).then(function (response) {
                ctrl.cardsByMilestone = response.milestones;
                ctrl.cardsCountByStatus = response.cardsCountByStatus;
                ctrl.statusColors = response.statusColors;
            });
        }

    }
})();
