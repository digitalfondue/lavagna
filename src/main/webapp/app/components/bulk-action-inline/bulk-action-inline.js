(function () {

	'use strict';

	angular.module('lavagna.components').component('lvgBulkActionInline', {
			templateUrl: 'app/components/bulk-action-inline/bulk-action-inline.html',
			bindings: {
				mode: '=',
				formatBulkRequest: '=',
				selectedVisibleCardsIdByColumnId : '=',
				afterBulkCompletion : '='
			},
			controllerAs: 'lvgBulkActionInline',
			controller: function($stateParams, Label, LabelCache, BulkOperations, Card) {
				
				var ctrl = this;
				
				//ugly
				if($stateParams.projectName !== undefined) {
					LabelCache.findByProjectShortName($stateParams.projectName).then(function(res) {
						for(var k in res) {
	  					if(res[k].domain === 'SYSTEM' && res[k].name === 'MILESTONE') {
	  						ctrl.milestoneLabel = res[k];
	  						break;
	  					}
	  				}
	        		  
	        		  
					ctrl.userLabels = [];
	        		 	for(var k in res) {
	        		  		if(res[k].domain === 'USER') {
	        		  			ctrl.userLabels.push(res[k]);
	        		  		}
	        		  	}
					});
				  }
				  
				  
				ctrl.confirmMove = function(location) {
					  var selectedByColumnId = ctrl.selectedVisibleCardsIdByColumnId();
					  for(var columnId in selectedByColumnId) {
			    			Card.moveAllFromColumnToLocation(columnId, selectedByColumnId[columnId], location);
					  }
				  };
				  
				  var applyIfPresent = function() {
        			  if(ctrl.afterBulkCompletion) {
        				  ctrl.afterBulkCompletion();
        			  }
        		  };
				  
	        	  //
        		  ctrl.assign = function(user) {
	        		  BulkOperations.assign(ctrl.formatBulkRequest(), user).then(applyIfPresent);
	        	  };

	        	  ctrl.removeAssign = function(user) {
	        		  BulkOperations.removeAssign(ctrl.formatBulkRequest(), user).then(applyIfPresent);
	        	  };

	        	  ctrl.reassign = function(user) {
	        		  BulkOperations.reassign(ctrl.formatBulkRequest(), user).then(applyIfPresent);
	        	  };

	        	  ctrl.setDueDate = function(dueDate) {
	        		  BulkOperations.setDueDate(ctrl.formatBulkRequest(), dueDate).then(applyIfPresent);
	        	  };

	        	  ctrl.removeDueDate = function() {
	        		  BulkOperations.removeDueDate(ctrl.formatBulkRequest()).then(applyIfPresent);
	        	  };

	        	  ctrl.setMilestone = function(milestone) {
	        		  BulkOperations.setMilestone(ctrl.formatBulkRequest(), milestone).then(applyIfPresent);
	        	  };

	        	  ctrl.removeMilestone = function() {
	        		  BulkOperations.removeMilestone(ctrl.formatBulkRequest()).then(applyIfPresent);
	        	  };
	        	  
	        	  ctrl.addLabel = function(labelToAdd) {
	        		  var labelValueToAdd = Label.extractValue(labelToAdd.label, labelToAdd.value);
	        		  BulkOperations.addLabel(ctrl.formatBulkRequest(), labelToAdd.label, labelValueToAdd).then(applyIfPresent);
	        	  };
	        	  
	        	  ctrl.removeLabel = function(toRemoveLabel) {
	        		  BulkOperations.removeLabel(ctrl.formatBulkRequest(), toRemoveLabel).then(applyIfPresent);
	        	  };
	          }
		
	});
})();