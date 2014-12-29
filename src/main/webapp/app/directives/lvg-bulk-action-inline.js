(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgBulkActionInline', function () {	
		return {
			templateUrl: 'partials/fragments/bulk-action-inline.html',
			restrict: 'E',
			scope: {
				mode: '=',
				formatBulkRequest: '=',
				selectedVisibleCardsIdByColumnId : '=',
				afterBulkCompletion : '='
			},
			
			controller: function($stateParams, $scope, Label, LabelCache, BulkOperations, Card) {
				//ugly
				if($stateParams.projectName !== undefined) {
					LabelCache.findByProjectShortName($stateParams.projectName).then(function(res) {
						for(var k in res) {
	  					if(res[k].domain === 'SYSTEM' && res[k].name === 'MILESTONE') {
	  						$scope.milestoneLabel = res[k];
	  						break;
	  					}
	  				}
	        		  
	        		  
	        		$scope.userLabels = [];
	        		 	for(var k in res) {
	        		  		if(res[k].domain === 'USER') {
	        		  			$scope.userLabels.push(res[k]);
	        		  		}
	        		  	}
					});
				  }
				  
				  
				  $scope.confirmMove = function(location) {
					  var selectedByColumnId = $scope.selectedVisibleCardsIdByColumnId();
					  for(var columnId in selectedByColumnId) {
			    			Card.moveAllFromColumnToLocation(columnId, selectedByColumnId[columnId], location);
					  }
				  };
				  
				  var applyIfPresent = function() {
        			  if($scope.afterBulkCompletion) {
        				  $scope.afterBulkCompletion();
        			  }
        		  };
				  
	        	  //
	        	  $scope.assign = function(user) {
	        		  BulkOperations.assign($scope.formatBulkRequest(), user).then(applyIfPresent);
	        	  };

	        	  $scope.removeAssign = function(user) {
	        		  BulkOperations.removeAssign($scope.formatBulkRequest(), user).then(applyIfPresent);
	        	  };

	        	  $scope.reassign = function(user) {
	        		  BulkOperations.reassign($scope.formatBulkRequest(), user).then(applyIfPresent);
	        	  };

	        	  $scope.setDueDate = function(dueDate) {
	        		  BulkOperations.setDueDate($scope.formatBulkRequest(), dueDate).then(applyIfPresent);
	        	  };

	        	  $scope.removeDueDate = function() {
	        		  BulkOperations.removeDueDate($scope.formatBulkRequest()).then(applyIfPresent);
	        	  };

	        	  $scope.setMilestone = function(milestone) {
	        		  BulkOperations.setMilestone($scope.formatBulkRequest(), milestone).then(applyIfPresent);
	        	  };

	        	  $scope.removeMilestone = function() {
	        		  BulkOperations.removeMilestone($scope.formatBulkRequest()).then(applyIfPresent);
	        	  };
	        	  
	        	  $scope.addLabel = function(labelToAdd) {
	        		  var labelValueToAdd = Label.extractValue(labelToAdd.label, labelToAdd.value);
	        		  BulkOperations.addLabel($scope.formatBulkRequest(), labelToAdd.label, labelValueToAdd).then(applyIfPresent);
	        	  };
	        	  
	        	  $scope.removeLabel = function(toRemoveLabel) {
	        		  BulkOperations.removeLabel($scope.formatBulkRequest(), toRemoveLabel).then(applyIfPresent);
	        	  };
	          }
		}
	});
})();