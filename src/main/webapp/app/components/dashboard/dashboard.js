(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentDashboard', {
    	bindings: {
    		user:'<'
    	},
        controller: DashboardController,
        controllerAs: 'dashboardCtrl',
        templateUrl: 'app/components/dashboard/dashboard.html'
    });

    function DashboardController($scope, $mdDialog, Project, User, Notification, StompClient) {

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
        
        ctrl.metadatas = {};
        
        ctrl.checkMetadatas = function checkMetadatas(metadatas) {
        	if(!metadatas) {
        		return null;
        	}
        	var hash = '';
        	angular.forEach(metadatas, function(v, k) {
        		hash+= v.metadata ? v.metadata.hash : '';
        	});
        	return hash;
        };
        
        ctrl.wrapMetadatas = function wrapMetadatas(metadatas) {
        	if(!metadatas) {
        		return [];
        	}
        	
        	for(var k in metadatas) {
        		if(!metadatas[k].metadata) {
        			return [];
        		}
        	}
        	return [metadatas];
        };
        

        var loadUserCards = function(page) {
            User.isAuthenticated().then(function() {return User.hasPermission('SEARCH')}).then(function() {
                User.cards(page).then(function(cards) {
                    ctrl.userCards = cards.cards;
                    ctrl.totalOpenCards = cards.totalCards;
                    ctrl.cardsPerPage = cards.itemsPerPage;
                    
                    for(var i = 0;i < cards.cards.length; i++) {
                    	var projectShortName = cards.cards[i].projectShortName;
                    	if(!ctrl.metadatas[projectShortName]) {
                    		ctrl.metadatas[projectShortName] = {};
                    		Project.loadMetadataAndSubscribe(projectShortName, ctrl.metadatas[projectShortName], $scope);
                    	}
                    }
                });
            });
        };

        loadUserCards(ctrl.view.cardPage - 1);

        ctrl.fetchUserCardsPage = function(page) {
            loadUserCards(page - 1);
        };

        StompClient.subscribe($scope, '/event/project', loadProjects);

        
        
        ctrl.showProjectDialog = function($event) {
        	$mdDialog.show({
            	templateUrl: 'app/components/dashboard/add-project-dialog.html',
            	targetEvent: $event,
            	fullscreen:true,
            	controllerAs: 'projectDialogCtrl',
            	controller: function() {
            		var ctrl = this;
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
                    
                    ctrl.checkProjectShortName = function(val) {
                        if(val !== undefined && val !== null) {
                            Project.checkShortName(val).then(function(res) {
                                ctrl.view.checkedShortName = res;
                            });
                        } else {
                            ctrl.view.checkedShortName = undefined;
                        }
                    };
                    
                    ctrl.suggestProjectShortName = function(project) {
                        if(project == null ||project.name == null || project.name == "") {
                            return;
                        }
                        Project.suggestShortName(project.name).then(function(res) {
                            project.shortName = res.suggestion;
                            ctrl.checkProjectShortName(res.suggestion);
                        });
                    };
                    
                    ctrl.close = function() {
                    	$mdDialog.hide();
                    }
            	}
            });
        }
    }
})();
