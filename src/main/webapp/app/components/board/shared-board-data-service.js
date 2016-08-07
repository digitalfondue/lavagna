(function () {
    'use strict';
    
    
    var components = angular.module('lavagna.components');
    
    //used for transient drag and drop data
    components.service('SharedBoardDataService', function($rootScope) {
    	return {
    		startDrag: function startDrag() {
    			$rootScope.$emit('SharedBoardDataService.startDrag');
    		},
    		endDrag: function endDrag() {
    			$rootScope.$emit('SharedBoardDataService.endDrag');
    		},
    		listenToDragStart: function listenToStartDrag(callback) {
    			return $rootScope.$on('SharedBoardDataService.startDrag', callback);
    		},
    		listenToDragEnd: function listenToDragEnd(callback) {
    			return $rootScope.$on('SharedBoardDataService.endDrag', callback);
    		}
    	};
    })
    
    
})();