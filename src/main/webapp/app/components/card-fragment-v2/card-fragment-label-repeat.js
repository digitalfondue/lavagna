(function () {
	'use strict';
	// based from https://www.accelebrate.com/blog/angularjs-transclusion-part-1/
	angular.module('lavagna.directives').directive('lvgCardFragmentLabelRepeat', function() {
		return {
			restrict: 'A',
		    transclude: 'element',
		    priority: 1000,
		    terminal: true,
		    scope: {
		    	lvgCardFragmentLabelRepeat:'&',
		    	projectMetadataRef:'&'
		    },
		    compile: function($element, $attr) {
		    	return function($scope, $element, $attr, ctrl, $transclude) {
		    		var parent = $element.parent();
		    		var toRepeat = $scope.lvgCardFragmentLabelRepeat();
		    		var projectMetadata = $scope.projectMetadataRef();
		    		for(var i = 0; i < toRepeat.length; i++) {
			    		$transclude(function (clone, scope) {
			    			scope.value = toRepeat[i];
			    			scope.projectMetadata = projectMetadata;
			    			parent.append(clone);
			    		});
		    		}
		    	}
		    }
		}
	});
})();
	