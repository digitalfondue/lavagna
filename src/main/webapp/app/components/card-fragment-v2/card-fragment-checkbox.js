(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.directive('lvgCardFragmentCheckbox', CardFragmentCheckbox);
    
    
    function CardFragmentCheckbox() {
    	return {
    		restrict: 'A',
    		require:'^lvgCardFragmentV2',
    		scope:false,
    		link: function($scope, $element, $attrs, lvgCardFragmentV2Ctrl) {
    			
    			var card = lvgCardFragmentV2Ctrl.card;
    			
    			var selected = lvgCardFragmentV2Ctrl.selected;
    			
    			function isSelected() {
    				try  {
    					return lvgCardFragmentV2Ctrl.boardView ? (selected[card.columnId][card.id] === true) : (selected[card.projectShortName][card.id] === true);
    				} catch(e) {
    					return false;
    				}
    			};
    			
    			function updateCheckbox() {
    				$element.prop('checked', isSelected());
    			};
    			
    			updateCheckbox();
    			
    			$scope.$on('updatecheckbox', updateCheckbox);
    			
    			if(lvgCardFragmentV2Ctrl.boardView) {
    				
    				$element.on('click', function() {
        				//$ctrl.selected[$ctrl.card.columnId][$ctrl.card.id]
    					$scope.$applyAsync(function() {
    						selected[card.columnId] = selected[card.columnId] || {};
    						selected[card.columnId][card.id] = !selected[card.columnId][card.id]
    					});
        			});
        			
    			} else {
    				$element.on('click', function() {
    					//$ctrl.selected[$ctrl.card.projectShortName][$ctrl.card.id]
    					$scope.$applyAsync(function() {
    						selected[card.projectShortName] = selected[card.projectShortName] || {};
    						selected[card.projectShortName][card.id] = !selected[card.projectShortName][card.id]
    					});
    				});
    			}
    		}
    	}
    }

    
})();