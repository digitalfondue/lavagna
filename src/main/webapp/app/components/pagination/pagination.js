(function() {

    'use strict';

    var components = angular.module('lavagna.components');
    
    components.component('lvgPagination', {
    	templateUrl: 'app/components/pagination/pagination.html',
    	bindings: {
    		totalPages:'<',
    		currentPage:'<',
    		maxSize:'<',
    		changePage:'<'
    	},
    	controller: function() {
    		var ctrl = this;
    		
    		ctrl.pages = [];
    		for(var i = 1; i <= ctrl.totalPages;i++) {
    			ctrl.pages.push(i);
    		}
    		
    		
    		ctrl.goTo = function(page) {
    			console.log(page);
    			ctrl.changePage(page);
    		}
    	}
    });
})();