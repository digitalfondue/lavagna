(function () {

    var components = angular.module('lavagna.components');

    components.component('lvgProjectMilestone', {
        bindings: {
            project: '<',
            id:'<'
        },
        templateUrl: 'app/components/project/milestone/project-milestone.html',
        controller: ['Card', 'User', ProjectMilestoneController],
    });

    function ProjectMilestoneController(Card, User) {
        var ctrl = this;
        
        ctrl.showArray = showArray;
        
        ctrl.$onInit = function init() {
        	
            ctrl.currentPage = 1;
            ctrl.statuses = ["BACKLOG", "OPEN", "DEFERRED", "CLOSED"];
        	
        	 User.hasPermission('READ', ctrl.project.shortName).then(function () {
                 return Card.findCardsByMilestone(ctrl.project.shortName);
             }).then(function (response) {
            	 
            	 ctrl.statusColors = response.statusColors;
            	 
            	 var idToCheck = parseInt(ctrl.id);
            	 
            	 for(var i = 0; i < response.milestones.length;i++) {
            		 var milestone = response.milestones[i];
            		 if((!isNaN(idToCheck) && milestone.labelListValue.id === idToCheck)) {
            			 ctrl.milestone = milestone;
            			 break;
            		 }
            	 };
            	 
            	 if(ctrl.milestone) {
            		 ctrl.cardsCountByStatus = response.cardsCountByStatus[ctrl.milestone.labelListValue.id];
            		 
            		 loadMilestoneDetail();
            	 }
            	 
             });
        }
                
        function showArray(array) {
            if (!array) {
                return false;
            }
            return Object.keys(array).length > 0;
        }
        
        function loadMilestoneDetail() {
            User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Card.findCardsByMilestoneDetail(ctrl.project.shortName, ctrl.milestone.labelListValue.id, ctrl.currentPage - 1).then(function (detail) {
                	ctrl.detail = detail;
                });
            });
        }
    }
})();
