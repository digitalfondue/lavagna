(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentDashboard', {
        controller: DashboardController,
        controllerAs: 'dashboardCtrl',
        templateUrl: 'app/components/dashboard/dashboard.html'
    });

    function DashboardController($scope, Project, User, Notification, StompClient) {

        var ctrl = this;

        ctrl.view = {};

        ctrl.view.projectPage = 1;
        ctrl.view.projectsPerPage = 10;
        ctrl.view.maxVisibleProjectPages = 3;

        var loadProjects = function() {
            Project.list().then(function(projects) {
                ctrl.projects = projects;
                ctrl.totalProjects = projects.length;
                ctrl.switchProjectPage(ctrl.view.projectPage);
            });
        };

        loadProjects();

        ctrl.switchProjectPage = function(page) {
            ctrl.currentProjects = ctrl.projects.slice((page - 1) * ctrl.view.projectsPerPage,
                    ((page - 1) * ctrl.view.projectsPerPage) + ctrl.view.projectsPerPage);
        };

        ctrl.view.cardPage = 1;
        ctrl.view.maxVisibleCardPages = 3;

        var loadUserCards = function(page) {
            User.isAuthenticated().then(function() {return User.hasPermission('SEARCH')}).then(function() {
                User.cards(page).then(function(cards) {
                    ctrl.userCards = cards.cards;
                    ctrl.totalOpenCards = cards.totalCards;
                    ctrl.cardsPerPage = cards.itemsPerPage;
                });
            });
        };

        loadUserCards(ctrl.view.cardPage - 1);

        ctrl.fetchUserCardsPage = function(page) {
            loadUserCards(page - 1);
        };

        StompClient.subscribe($scope, '/event/project', loadProjects);

        ctrl.suggestProjectShortName = function(project) {
            if(project == null ||project.name == null || project.name == "") {
                return;
            }
            Project.suggestShortName(project.name).then(function(res) {
                project.shortName = res.suggestion;
                ctrl.checkProjectShortName(res.suggestion);
            });
        };

        ctrl.checkProjectShortName = function(val) {
            if(val !== undefined && val !== null) {
                Project.checkShortName(val).then(function(res) {
                    ctrl.view.checkedShortName = res;
                });
            } else {
                ctrl.view.checkedShortName = undefined;
            }
        };


        ctrl.createProject = function(project) {
            project.shortName = project.shortName.toUpperCase();
            Project.create(project).then(function() {
                ctrl.view.project = {};
                ctrl.view.checkedShortName = undefined;
                Notification.addAutoAckNotification('success', {key: 'notification.project.creation.success'}, false);
            }, function(error) {
                Notification.addAutoAckNotification('error', {key: 'notification.project.creation.error'}, false);
            });
        };
    }
})();
