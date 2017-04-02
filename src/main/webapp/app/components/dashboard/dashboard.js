(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgDashboard', {
        bindings: {
            user: '<'
        },
        templateUrl: 'app/components/dashboard/dashboard.html',
        controller: ['$mdDialog', 'Project', 'User', 'Notification', 'StompClient', DashboardController],
    });

    function DashboardController($mdDialog, Project, User, Notification, StompClient) {
        var ctrl = this;

        ctrl.fetchUserCardsPage = fetchUserCardsPage;
        ctrl.getMetadatasHash = getMetadatasHash;
        ctrl.showProjectDialog = showProjectDialog;
        ctrl.updateShowArchivedItems = updateShowArchivedItems;

        //

        var onDestroyStomp = angular.noop;
        var projectMetadataSubscriptions = [];

        ctrl.$onInit = function init() {
            ctrl.view = {
                projectPage: 1,
                projectsPerPage: 10,
                maxVisibleProjectPages: 3,
                cardPage: 1
            };

            ctrl.metadatas = {};

            onDestroyStomp = StompClient.subscribe('/event/project', loadProjects);

            loadProjects();
            loadUserCards(ctrl.view.cardPage - 1);

            ctrl.showArchivedItems = ctrl.user.userMetadata && ctrl.user.userMetadata.showArchivedProjects || false;
        };

        ctrl.$onDestroy = function onDestroy() {
            onDestroyStomp();
            angular.forEach(projectMetadataSubscriptions, function (subscription) {
                subscription();
            });
        };

        function loadProjects() {
            Project.list().then(function (projects) {
                ctrl.projects = projects;
            });
        }

        function loadUserCards(page) {
            User.isAuthenticated().then(function () { return User.hasPermission('SEARCH'); }).then(function () {
                User.cards(page).then(function (cards) {
                    ctrl.userCards = cards.found.slice(0, cards.countPerPage);
                    ctrl.totalOpenCards = cards.count;
                    ctrl.cardsCurrentPage = cards.currentPage + 1;
                    ctrl.cardsTotalPages = cards.totalPages;
                    for (var i = 0;i < ctrl.userCards.length; i++) {
                        var projectShortName = ctrl.userCards[i].projectShortName;

                        if (!ctrl.metadatas[projectShortName]) {
                            ctrl.metadatas[projectShortName] = {};
                            projectMetadataSubscriptions.push(Project.loadMetadataAndSubscribe(projectShortName, ctrl.metadatas, true));
                        }
                    }
                });
            });
        }

        function fetchUserCardsPage(page) {
            loadUserCards(page - 1);
        }

        function getMetadatasHash() {
            var hash = '';

            for (var k in ctrl.metadatas) {
                if (ctrl.metadatas[k].hash) {
                    hash += ctrl.metadatas[k].hash;
                }
            }

            return hash;
        }

        function showProjectDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/dashboard/add-project-dialog.html',
                fullscreen: true,
                controllerAs: 'projectDialogCtrl',
                controller: function () {
                    var ctrl = this;

                    ctrl.errors = {};

                    ctrl.createProject = function (project) {
                        project.shortName = project.shortName.toUpperCase();
                        Project.create(project).then(function () {
                            Notification.addAutoAckNotification('success', {key: 'notification.project.creation.success'}, false);
                            ctrl.close();
                        }, function () {
                            Notification.addAutoAckNotification('error', {key: 'notification.project.creation.error'}, false);
                        });
                    };

                    ctrl.checkProjectShortName = function (val) {
                        if (val !== undefined && val !== null) {
                            Project.checkShortName(val).then(function (res) {
                                ctrl.errors.shortName = res === false ? true : undefined;
                            });
                        } else {
                            ctrl.errors.shortName = undefined;
                        }
                    };

                    ctrl.suggestProjectShortName = function (project) {
                        if (project === null || !angular.isDefined(project.name) || project.name === null || project.name === '') {
                            return;
                        }
                        Project.suggestShortName(project.name).then(function (res) {
                            project.shortName = res.suggestion;
                            ctrl.checkProjectShortName(res.suggestion);
                        });
                    };

                    ctrl.close = function () {
                        $mdDialog.hide();
                    };
                }
            });
        }

        function updateShowArchivedItems(value) {
            var metadata = ctrl.user.userMetadata || {};

            metadata.showArchivedProjects = value;
            User.updateMetadata(metadata).then(User.current, User.current).then(function (user) {
                ctrl.user = user;
                ctrl.showArchivedItems = user.userMetadata.showArchivedProjects;
            });
        }
    }
}());
