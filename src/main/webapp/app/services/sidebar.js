(function () {

	'use strict';

	var services = angular.module('lavagna.services');

	services.factory('Sidebar', function($rootScope) {

	    var openHandlers = [];
	    var closeHandlers = [];
	    var open = false;

	    var callHandlers = function(handlers) {
	        for(var i = 0; i < handlers.length; i++) {
                if(typeof handlers[i] === 'function') {
                    handlers[i]();
                }
	        }
	    }

        return {
            toggle: function() {
                open ? this.close() : this.open();
            },
            open: function() {
                if(!open) {
                    open = true;
                    callHandlers(openHandlers);
                }
            },
            close: function() {
                if(open) {
                    open = false;
                    callHandlers(closeHandlers);
                }
            },
            onOpen: function(funct) {
                openHandlers.push(funct);
            },

            onClose: function(funct) {
                closeHandlers.push(funct);
            },

            unbindOpen: function(funct) {
                var idx = openHandlers.indexOf(funct);
                if(idx != -1) {
                    openHandlers.slice(idx,1);
                }
            },

            unbindClose: function(funct) {
                var idx = closeHandlers.indexOf(funct);
                if(idx != -1) {
                    closeHandlers.slice(idx,1);
                }
            }
        }
	});

})();
