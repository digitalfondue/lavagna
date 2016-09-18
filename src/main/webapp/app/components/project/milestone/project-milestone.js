(function () {

    var components = angular.module('lavagna.components');

    components.component('lvgProjectMilestone', {
        bindings: {
            project: '<',
            id:'<'
        },
        templateUrl: 'app/components/project/milestone/project-milestone.html',
        controller: ['Card', 'User', 'BoardCache', ProjectMilestoneController],
    });

    function ProjectMilestoneController(Card, User, BoardCache) {
        var ctrl = this;
        
        ctrl.showArray = showArray;
        ctrl.loadColumn = loadColumn;
        
        ctrl.$onInit = function init() {
        	
        	
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
        
        function loadColumn(card) {
            BoardCache.column(card.columnId).then(function (col) {
                card.column = col;
            });
        }
                
        function showArray(array) {
            if (!array) {
                return false;
            }
            return Object.keys(array).length > 0;
        }
        
        function loadMilestoneDetail(page) {
            return User.hasPermission('READ', ctrl.project.shortName).then(function () {
                return Card.findCardsByMilestoneDetail(ctrl.project.shortName, ctrl.milestone.labelListValue.id).then(function (detail) {
                	ctrl.detail = detail;
                });
            });
        }
    }
})();
