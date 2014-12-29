/*
 from https://raw.github.com/thgreasi/ui-sortable/master/src/sortable.js and heavily
 modified for our need :
 	- removed the model handling
 	- when the configuration has been set, the watcher is removed).
  
 jQuery UI Sortable plugin wrapper

 @param [ui-sortable] {object} Options to pass to $.fn.sortable() merged onto ui.config
*/
angular.module('ui.sortable', [])
.value('uiSortableConfig',{})
.directive('uiSortable', [ 'uiSortableConfig', function(uiSortableConfig) {
        return {
          link: function(scope, element, attrs) {
        	  function combineCallbacks(first,second){
        		  if( second && (typeof second === "function") ){
        			  return function(e,ui){
        				  first(e,ui);
        				  second(e,ui);
        			  };
                  }
                  return first;
              }

        	  var opts = {};

        	  var callbacks = {
        			  receive: null,
        			  remove:null,
        			  start:null,
        			  stop:null,
        			  update:null
        	  };

        	  angular.extend(opts, uiSortableConfig);

              var unregisterWatcher = scope.$watch(attrs.uiSortable, function(newVal, oldVal){
            	  if(newVal === undefined) {
            		  return;
            	  }
            	  
            	  if(newVal === false) {
            		  element.sortable('destroy');
            		  unregisterWatcher();
            		  return;
            	  }
            	  
            	  //--------
                  angular.forEach(newVal, function(value, key){
                      if( callbacks[key] ){
                          // wrap the callback
                          value = combineCallbacks(callbacks[key], value );
                      }
                      element.sortable('option', key, value);
                  });
                  
                  unregisterWatcher();
              }, true);

              angular.forEach(callbacks, function(value, key ){
            	  opts[key] = combineCallbacks(value, opts[key]);
              });

              // Create sortable
              element.sortable(opts);
          }
        };
      }
]);
