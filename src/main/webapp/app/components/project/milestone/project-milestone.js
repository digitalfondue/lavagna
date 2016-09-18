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
        
        ctrl.$onInit = function init() {
        	 User.hasPermission('READ', ctrl.project.shortName).then(function () {
                 return Card.findCardsByMilestone(ctrl.project.shortName);
             }).then(function (response) {
            	 
            	 ctrl.statusColors = response.statusColors;
            	 
            	 var idToCheck = parseInt(ctrl.id);
            	 
            	 for(var i = 0; i < response.milestones.length;i++) {
            		 var milestone = response.milestones[i];
            		 if((!isNaN(idToCheck) && milestone.labelListValue.id === idToCheck) || (ctrl.id === 'Unassigned' && isUnassigned(milestone))) {
            			 ctrl.milestone = milestone;
            			 break;
            		 }
            	 };
            	 
            	 if(ctrl.milestone) {
            		 ctrl.cardsCountByStatus = response.cardsCountByStatus[ctrl.milestone.labelListValue.id];
            	 }
             });
        }
        
        function isUnassigned(milestone) {
        	return milestone.labelListValue.id === -1 && milestone.labelListValue.order === 2147483647 && milestone.labelListValue.value === 'Unassigned';
        }
    }
})();
