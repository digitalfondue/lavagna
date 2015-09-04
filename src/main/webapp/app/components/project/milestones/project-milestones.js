(function () {

    var components = angular.module('lavagna.components');

    components.directive('lvgComponentProjectMilestones', ProjectMilestonesComponent);

    function ProjectMilestonesComponent(Card, User, Label, Notification, StompClient) {
        return {
            restrict: 'E',
            scope: true,
            controller: ProjectMilestonesController,
            controllerAs: 'projectMilestonesCtrl',
            bindToController: {
                project: '='
            },
            templateUrl: 'app/components/project/milestones/project-milestones.html'
        }
    }

    function ProjectMilestonesController($scope, Card, User, Label, Notification, StompClient) {
        var projectMilestonesCtrl = this;

        projectMilestonesCtrl.milestoneOpenStatus = {};

        projectMilestonesCtrl.showArray = function (array, minLength) {
            if (!array) {
                return false;
            }
            return Object.keys(array).length > minLength;
        };

        projectMilestonesCtrl.closeMilestone = function(val) {
            Label.createLabelListValueMetadata(val.id, 'status', 'CLOSED');
        };

        projectMilestonesCtrl.openMilestone = function(val) {
            Label.removeLabelListValueMetadata(val.id, 'status');
        };

        projectMilestonesCtrl.orderCardByStatus = function(card) {
            return card.columnDefinition == "CLOSED" ? 1 : 0;
        }

        var orderByStatus = function (milestone) {
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
            projectMilestonesCtrl.cardsCountByStatus[milestone.labelListValue.value] = sorted;
        };

        projectMilestonesCtrl.moveDetailToPage = function (milestone, page) {
            User.hasPermission('READ', projectMilestonesCtrl.project.shortName).then(function () {
                return Card.findCardsByMilestoneDetail(projectMilestonesCtrl.project.shortName, milestone.labelListValue.value);
            }).then(function (response) {
                milestone.detail = response;
                milestone.currentPage = page + 1;
            });
        };

        var loadMilestonesInProject = function () {
            User.hasPermission('READ', projectMilestonesCtrl.project.shortName).then(function () {
                return Card.findCardsByMilestone(projectMilestonesCtrl.project.shortName);
            }).then(function (response) {
                projectMilestonesCtrl.cardsByMilestone = response.milestones;
                projectMilestonesCtrl.cardsCountByStatus = [];
                for (var index in response.milestones) {
                    var milestone = response.milestones[index];
                    orderByStatus(milestone);
                    if (projectMilestonesCtrl.milestoneOpenStatus[milestone.labelListValue.value]) {
                        projectMilestonesCtrl.moveDetailToPage(milestone, 0);
                    }
                }
                projectMilestonesCtrl.statusColors = response.statusColors;
            });
        };

        loadMilestonesInProject();

        StompClient.subscribe($scope, '/event/project/' + projectMilestonesCtrl.project.shortName + '/label-value', loadMilestonesInProject);

        StompClient.subscribe($scope, '/event/project/' + projectMilestonesCtrl.project.shortName + '/label', loadMilestonesInProject);

        projectMilestonesCtrl.clearMilestoneDetail = function (milestone) {
            milestone.detail = null;
            milestone.currentPage = 1;
        };

        projectMilestonesCtrl.loadMilestoneDetail = function (milestone) {
            projectMilestonesCtrl.moveDetailToPage(milestone, 0);
        };

        projectMilestonesCtrl.toggleMilestoneOpenStatus = function (milestone) {
            var currentOpenStatus = projectMilestonesCtrl.milestoneOpenStatus[milestone.labelListValue.value];
            currentOpenStatus ? projectMilestonesCtrl.clearMilestoneDetail(milestone) : projectMilestonesCtrl.loadMilestoneDetail(milestone);
            projectMilestonesCtrl.milestoneOpenStatus[milestone.labelListValue.value] = !currentOpenStatus;
        };

        projectMilestonesCtrl.updateMilestone = function (milestone, newName) {
            var newLabelValue = jQuery.extend({}, milestone.labelListValue);
            newLabelValue.value = newName;
            Label.updateLabelListValue(newLabelValue).catch(function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-milestones.update.error'}, false);
            });
        };
    }
})();
