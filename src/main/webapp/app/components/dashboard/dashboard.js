(function() {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgDashboard', {
    	bindings: {
    		user:'<'
    	},
        controller: DashboardController,
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
            });
        };

        loadProjects();


        ctrl.view.cardPage = 1;
        
        
        ctrl.metadatas = {};

        var loadUserCards = function(page) {
            User.isAuthenticated().then(function() {return User.hasPermission('SEARCH')}).then(function() {
                User.cards(page).then(function(cards) {
                    
                	
                	ctrl.userCards = cards.found.slice(0, cards.countPerPage);
                    ctrl.totalOpenCards = cards.count;
                    ctrl.cardsCurrentPage = cards.currentPage+1;
					ctrl.cardsTotalPages = cards.totalPages;
                    
                    for(var i = 0;i < ctrl.userCards.length; i++) {
                    	var projectShortName = ctrl.userCards[i].projectShortName;
                    	if(!ctrl.metadatas[projectShortName]) {
                    		ctrl.metadatas[projectShortName] = {};
                    		Project.loadMetadataAndSubscribe(projectShortName, ctrl.metadatas, $scope, true);
                    	}
                    }
                });
            });
        };

        loadUserCards(ctrl.view.cardPage - 1);

        ctrl.fetchUserCardsPage = function(page) {
            loadUserCards(page - 1);
        };
        
        // 
        var r = []
        ctrl.getMetadatasHash = function getMetadatasHash() {
        	var hash = '';
        	for(var k in ctrl.metadatas) {
        		if(ctrl.metadatas[k].hash) {
        			hash+=ctrl.metadatas[k].hash;
        		}
        	}
        	r[0] = hash;
        	return r;
        }
        //

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
