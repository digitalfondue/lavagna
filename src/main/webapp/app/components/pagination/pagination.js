(function() {

    'use strict';

    var components = angular.module('lavagna.components');
    
    components.component('lvgPagination', {
    	templateUrl: 'app/components/pagination/pagination.html',
    	bindings: {
    		totalPages:'<',
    		currentPage:'<',
    		maxSize:'<',
    		changePage:'&' // $page parameter
    	},
    	controller: function() {
    		var ctrl = this;
    		
    		ctrl.$onChanges = function(change) {
				ctrl.pages = [];
	    		for(var i = 1; i <= ctrl.totalPages;i++) {
	    			ctrl.pages.push(i);
	    		}
    		}
    	}
    });
})();