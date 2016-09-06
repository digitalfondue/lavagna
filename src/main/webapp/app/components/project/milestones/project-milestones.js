(function () {

    var components = angular.module('lavagna.components');

    components.component('lvgProjectMilestones', {
        bindings: {
            project: '<'
        },
        templateUrl: 'app/components/project/milestones/project-milestones.html',
        controller: ['$rootScope', 'Card', 'User', 'Label', 'Notification', 'StompClient', ProjectMilestonesController],
    });

    function ProjectMilestonesController($rootScope, Card, User, Label, Notification, StompClient) {
        var ctrl = this;
        //
        ctrl.showArray = showArray;
    	ctrl.closeMilestone = closeMilestone;
		ctrl.openMilestone = openMilestone;
		ctrl.orderCardByStatus = orderCardByStatus;
		ctrl.moveDetailToPage = moveDetailToPage;
		ctrl.clearMilestoneDetail = clearMilestoneDetail;
		ctrl.loadMilestoneDetail = loadMilestoneDetail;
    	ctrl.toggleMilestoneOpenStatus = toggleMilestoneOpenStatus;
    	ctrl.updateMilestone = updateMilestone;
		ctrl.updateMilestoneDate = updateMilestoneDate;
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
	        unbindMovedEvent =  $rootScope.$on('card.moved.event', loadMilestonesInProject);
	        unbindRenamedEvent =  $rootScope.$on('card.renamed.event', loadMilestonesInProject);
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

        function closeMilestone(val) {
            Label.updateLabelListValueMetadata(val.id, 'status', 'CLOSED');
        }

        function openMilestone(val) {
            Label.removeLabelListValueMetadata(val.id, 'status');
        }

        function orderCardByStatus(card) {
            return card.columnDefinition == "CLOSED" ? 1 : 0;
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

        function moveDetailToPage(milestone, page) {
            User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Card.findCardsByMilestoneDetail(ctrl.project.shortName, milestone.labelListValue.value);
            }).then(function (response) {
                milestone.detail = response;
            });
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
                    if (ctrl.milestoneOpenStatus[milestone.labelListValue.value]) {
                        ctrl.moveDetailToPage(milestone, 0);
                    }
                }
                ctrl.statusColors = response.statusColors;
            });
        }
        
        function clearMilestoneDetail(milestone) {
            milestone.detail = null;
            milestone.currentPage = 1;
        }

        function loadMilestoneDetail(milestone) {
            ctrl.moveDetailToPage(milestone, 0);
        }

        function toggleMilestoneOpenStatus(milestone) {
            var currentOpenStatus = ctrl.milestoneOpenStatus[milestone.labelListValue.value];
            currentOpenStatus ? ctrl.clearMilestoneDetail(milestone) : ctrl.loadMilestoneDetail(milestone);
            ctrl.milestoneOpenStatus[milestone.labelListValue.value] = !currentOpenStatus;
        }

        function updateMilestone(milestone, newName) {
            var newLabelValue = angular.extend({}, milestone.labelListValue);
            newLabelValue.value = newName;
            Label.updateLabelListValue(newLabelValue).catch(function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-milestones.update.error'}, false);
            });
        }

        function updateMilestoneDate(milestoneId, newDate) {
            if (newDate) {
                Label.updateLabelListValueMetadata(milestoneId, 'releaseDate', newDate);
            } else {
                Label.removeLabelListValueMetadata(milestoneId, 'releaseDate');
            }
        }

    }
})();
