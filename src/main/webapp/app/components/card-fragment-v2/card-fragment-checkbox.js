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
    			
    			if(lvgCardFragmentV2Ctrl.boardView) {
    				
    				var card = lvgCardFragmentV2Ctrl.card;
    				
    				$element.on('click', function() {
        				//$ctrl.selected[$ctrl.card.columnId][$ctrl.card.id]
    					$scope.$applyAsync(function() {
        					lvgCardFragmentV2Ctrl.selected[card.columnId] = lvgCardFragmentV2Ctrl.selected[card.columnId] || {};
        					lvgCardFragmentV2Ctrl.selected[card.columnId][card.id] = !lvgCardFragmentV2Ctrl.selected[card.columnId][card.id]
    					});
        			});
        			
        			
        			$scope.$on('updatecheckbox', updateCheckbox);
        			
        			function updateCheckbox() {
        				$element.prop('checked', isSelected());
        			}
        			
        			function isSelected() {
        				try  {
        					return lvgCardFragmentV2Ctrl.selected[card.columnId][card.id]
        				} catch(e) {
        					return false;
        				}
        			}
        			
        			updateCheckbox();
        			
    			} else {
    				
    			}
    		}
    	}
    }

    
})();