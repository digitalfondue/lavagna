(function () {

	'use strict';

	const directives = angular.module('lavagna.directives');
	
	//based on angular.js code for ngIfDirective https://github.com/angular/angular.js/blob/master/src/ng/directive/ngIf.js#L104

	directives.directive('lvgIfInstant', ['$parse', function ($parse) {
		return {
		    transclude: 'element',
		    priority: 600,
		    terminal: true,
		    restrict: 'A',
			link: function lvgIfInstantLink($scope, $element, $attr, ctrl, $transclude) {
				const attrVal = $attr.lvgIfInstant;
				const parsed = $parse(attrVal);
				
				if(parsed($scope)) {
					$transclude(function(clone, newScope) {
						enter(clone, $element.parent(), $element)
					});
				} else {
					$element.remove();
				}
			}
		};
	}]);
	
	
	function enter(element, parent, after) {
		parent = parent && angular.element(parent);
        after = after && angular.element(after);
        parent = parent || after.parent();
        domInsert(element, parent, after);
	}
	
	
	
	function domInsert(element, parentElement, afterElement) {
	      // if for some reason the previous element was removed
	      // from the dom sometime before this code runs then let's
	      // just stick to using the parent element as the anchor
	      if (afterElement) {
	        var afterNode = extractElementNode(afterElement);
	        if (afterNode && !afterNode.parentNode && !afterNode.previousElementSibling) {
	          afterElement = null;
	        }
	      }
	      afterElement ? afterElement.after(element) : parentElement.prepend(element);
	    }
	
	function extractElementNode(element) {
		  for (var i = 0; i < element.length; i++) {
		    var elm = element[i];
		    if (elm.nodeType === 1) { //var ELEMENT_NODE = 1;
		      return elm;
		    }
		  }
		}
	
})();