(function () {

    'use strict';
    
    angular.module('lavagna.components').component('lvgSearchResult', {
        templateUrl: 'app/components/search-result/search-result.html',
        bindings: {
        	count: '=',
        	page: '=',
        	moveToPage: '=',
        	found: '=',
        	project: '=',
        	selected: '=',
        	cardFragmentDependencies: '='
        }
    });
    
})();