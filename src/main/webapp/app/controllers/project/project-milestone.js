(function () {

    'use strict';

    var module = angular.module('lavagna.controllers');

    module.controller('ProjectMilestoneCtrl', function ($rootScope, $scope, $state,
                                                        project, milestone,
                                                        Label, LabelCache, Card, User, StompClient) {

        $scope.sidebarOpen = true;
        $scope.project = project;
        $scope.milestone = milestone;
        $scope.totalCards = 0;
        $scope.currentPage = 1;
        $scope.statuses = ["BACKLOG", "OPEN", "DEFERRED", "CLOSED"];

        var loadMilestoneDetail = function () {
            User.hasPermission('READ', project.shortName).then(function () {
                return Card.findCardsByMilestoneDetail(project.shortName, milestone.value).then(function (detail) {
                    $scope.detail = detail;
                });
            });
        };

        var reloadAll = function () {

            LabelCache.findLabelListValue($scope.milestone.cardLabelId, $scope.milestone.id).then(function (m) {

                if (m.value !== milestone.value) {
                    $state.go('projectMilestone', {projectName: project.shortName, milestone: m.value});
                } else {
// TODO FIXME
                }
            });

        };

        loadMilestoneDetail();

        StompClient.subscribe($scope, '/event/project/' + project.shortName + '/label-value', reloadAll);

        StompClient.subscribe($scope, '/event/project/' + project.shortName + '/label', reloadAll);

        var unbindMovedEvent = $rootScope.$on('card.moved.event', loadMilestoneDetail);
        $scope.$on('$destroy', unbindMovedEvent);

        var unbindRenamedEvent = $rootScope.$on('card.renamed.event', loadMilestoneDetail);
        $scope.$on('$destroy', unbindRenamedEvent);


        $scope.closeMilestone = function () {
            Label.updateLabelListValueMetadata($scope.milestone.id, 'status', 'CLOSED');
        };

        $scope.openMilestone = function () {
            Label.removeLabelListValueMetadata($scope.milestone.id, 'status');
        };


        $scope.orderCardByStatus = function (card) {
            return card.columnDefinition == "CLOSED" ? 1 : 0;
        };
        /*
         $scope.moveDetailToPage = function (milestone, page) {
         User.hasPermission('READ', $stateParams.projectName).then(function () {
         return Card.findCardsByMilestoneDetail($stateParams.projectName, milestone.labelListValue.value);
         }).then(function (response) {
         milestone.detail = response;
         milestone.currentPage = page + 1;
         });
         };
         */

        /*
         $scope.clearMilestoneDetail = function (milestone) {
         milestone.detail = null;
         milestone.currentPage = 1;
         };

         $scope.loadMilestoneDetail = function (milestone) {
         $scope.moveDetailToPage(milestone, 0);
         };

         $scope.toggleMilestoneOpenStatus = function (milestone) {
         var currentOpenStatus = $scope.milestoneOpenStatus[milestone.labelListValue.value];
         currentOpenStatus ? $scope.clearMilestoneDetail(milestone) : $scope.loadMilestoneDetail(milestone);
         $scope.milestoneOpenStatus[milestone.labelListValue.value] = !currentOpenStatus;
         };
         */

        $scope.updateMilestone = function (newName) {
            var newLabelValue = jQuery.extend({}, $scope.milestone);
            newLabelValue.value = newName;
            Label.updateLabelListValue(newLabelValue).then(function () {
                $state.go('projectMilestone', {projectName: project.shortName, milestone: newName});
            }).catch(function (error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project-milestones.update.error'}, false);
            });
        };


        $scope.updateMilestoneDate = function (newDate) {
            if (newDate) {
                Label.updateLabelListValueMetadata($scope.milestone.id, 'releaseDate', newDate);
            } else {
                Label.removeLabelListValueMetadata($scope.milestone.id, 'releaseDate');
            }
        }

    });
})();
