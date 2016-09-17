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

        function orderByStatus(milestone) {
            var insertStatusIfExists = function (milestone, source, target, status) {
                if (source[status] != undefined) {
                    target[target.length] = {status: status, count: source[status]};
                    milestone.totalCards += source[status];
                }
            };

            milestone.totalCards = 0;
            var sorted = [];
            insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "BACKLOG");
            insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "OPEN");
            insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "DEFERRED");
            insertStatusIfExists(milestone, milestone.cardsCountByStatus, sorted, "CLOSED");
            ctrl.cardsCountByStatus[milestone.labelListValue.value] = sorted;
        }

        function loadMilestonesInProject() {
            User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Card.findCardsByMilestone(ctrl.project.shortName);
            }).then(function (response) {
                ctrl.cardsByMilestone = response.milestones;
                ctrl.cardsCountByStatus = [];
                for (var index in response.milestones) {
                    var milestone = response.milestones[index];
                    orderByStatus(milestone);
                }
                ctrl.statusColors = response.statusColors;
            });
        }

    }
})();
