(function () {

    'use strict';
    
    angular.module('lavagna.components').component('lvgSearchResult', {
        templateUrl: 'app/components/search-result/search-result.html',
        bindings: {
        	count: '=',
        	page: '=',
        	query: '=',
        	moveToPage: '<',
        	found: '<',
        	project: '=',
        	selected: '=',
        	user: '<',
        	totalPages: '<',
        	countPerPage:'<',
        	currentPage:'<'
        },
        controllerAs: 'lvgSearchResult',
        controller: function(Project, $scope) {
        	var ctrl = this;
        	
        	
        	var projects = {};
        	
        	ctrl.metadatas = {};
        	
        	ctrl.$onChanges = function(changesObj) {
        		for(var i = 0; i < ctrl.found.length;i++) {
        			var card = ctrl.found[i];
        			if(!projects[card.projectShortName]) {
        				projects[card.projectShortName] = true;
        				Project.loadMetadataAndSubscribe(card.projectShortName, ctrl.metadatas, $scope, true);
        			}
        		}
        	}
        }
    });
    
})();