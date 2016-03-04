;

/**

   
 TODO:
 
- add support for html in autocomplete (add $sce) 
 
 - menu function triggers support
 - code whelp
 - autocomplete list default if the df-autocomplete-list directive is not present
 
 NOTES:
 
 dfAutocomplete has a isolated scope, this scope is shared with the transcluded html from the dfAutocompleteMenu and dfAutocompleteList
 directives.

*/

(function() {

  'use strict';
  
  function showAutocompleteList(input, result) {
	return (input !== null && input !== undefined && input.trim().length > 0) || (result);
  };
  
  //http://stackoverflow.com/a/2234986
	function isDescendant(parent, child) {
		var node = child.parentNode;
			while (node != null) {
				if (node == parent) {
					return true;
				}
			node = node.parentNode;
			}
		return false;
	}
  
  angular.module('digitalfondue.dfautocomplete', [])
  
  .directive('dfAutocomplete', ['$window', '$timeout', '$q', function($window, $timeout, $q) {
  
	return {
		restrict: 'E',
		template: '<div class="df-autocomplete" data-ng-class="{\'df-autocomplete-show-menu\': dfAutocompleteInternalState.showMenu, \'df-autocomplete-show-list\': dfAutocompleteInternalState.showAutocompleteList}" data-ng-focus="focused()" data-ng-blur="blurred()">'
			+'<div class="df-autocomplete-box">'
				+'<div class="df-autocomplete-input" data-ng-click="focusToInputField($event)"><div style="padding-left:6px;padding-right:6px">'
					+'<span ng-repeat="tag in ngModel.tags">'
						+'<input ng-if="dfAutocompleteInternalState.inputPosition == $index - ngModel.tags.length " '
							+'data-ng-init="initInput()" data-ng-blur="blurredInput()" '
							+'data-input-index="{{$index - ngModel.tags.length}}" '
							+'data-ng-model="ngModel.userInput" data-ng-trim="false" '
							+'class="df-autocomplete-user-input" data-ng-keydown="handleKeyDown($event)" '
							+'data-ng-focus="focusOnInput()" style="width:4px">'
						+'<div class="df-autocomplete-tag" df-autocomplete-tag-render></div>'
					+'</span>'
					+'<input ng-if="dfAutocompleteInternalState.inputPosition == 0" data-input-index="0" '
						+'data-ng-init="initInput()" data-ng-blur="blurredInput()" '
						+'data-ng-model="ngModel.userInput" data-ng-trim="false" '
						+'class="df-autocomplete-user-input" data-ng-keydown="handleKeyDown($event)" '
						+'data-ng-focus="focusOnInput()" style="width:4px">'
					+'<span class="df-autocomplete-input-size"></span>'
				+'</div></div>'
				+'<div class="df-autocomplete-controls" data-ng-if="dfAutocompleteInternalState.menuPresent"><span class="df-autocomplete-chevron" data-ng-click="toggleMenu()"></span></div>'
			+'</div>'
			+'<div data-ng-transclude></div></div>',
		scope: {dfAutocompleteConfiguration : '=?', ngModel: '=?', dfAutocompleteInternalState : '=?'},
		transclude: true,
		controller: function($scope) {
		
			$scope.ngModel = $scope.ngModel || {};
			$scope.ngModel.tags = $scope.ngModel.tags || [];
			

			$scope.dfAutocompleteConfiguration = $scope.dfAutocompleteConfiguration || {};
			$scope.dfAutocompleteConfiguration.tagTemplate = $scope.dfAutocompleteConfiguration.tagTemplate || '{{tag}}';
			
			$scope.dfAutocompleteInternalState = $scope.dfAutocompleteInternalState || {};
			
			
			var state = $scope.dfAutocompleteInternalState;
			
			state.inputPosition = 0;
			state.mustFocusOnInput = false;
			state.showAutocompleteList = false;
			state.showMenu = false;

			state.registeredInputChange = [];
			state.registeredAutocompleteChange = [];
			state.registeredKeyDown = [];
			
			state.autocompleteSelectionIndex = 0;

			var blurring = undefined;
			$scope.blurredInput = function() {
				//reset the input position only if the input field is empty
				if(!$scope.ngModel.userInput) {
					blurring = $timeout(function() {
						$scope.dfAutocompleteInternalState.inputPosition = 0
					}, 100);
				}
			};
			
			$scope.addTagInNaturalPosition = function(elem) {
				if(!angular.isArray($scope.ngModel.tags)) {
					$scope.ngModel.tags = [];
				}
				$scope.ngModel.tags.splice($scope.ngModel.tags.length + $scope.dfAutocompleteInternalState.inputPosition, 0, elem);
			};
			
			$scope.selected = function(elem) {
				if(angular.isFunction($scope.dfAutocompleteConfiguration.autocompleteSelection)) {
					$scope.dfAutocompleteConfiguration.autocompleteSelection(elem, $scope);
				}
				state.showMenu = false;
				state.showAutocompleteList = false;
								
				if(blurring) {
					$timeout.cancel(blurring);
					blurring = undefined;
				}
				
				$scope.focusToInputField();
			};
		
			$scope.toggleMenu = function() {
				state.showMenu = !state.showMenu;
				if(state.showMenu) {
					state.showAutocompleteList = false;
				}
			};
			
			$scope.removeTag = function(toRemove) {
				$scope.ngModel.tags.splice($scope.ngModel.tags.indexOf(toRemove), 1);
			};			
			
			var handleUserInput = function(newValue) {
			
				state.showMenu = false;
				state.showAutocompleteList = showAutocompleteList(newValue, null);
								
				for(var i = 0;i<state.registeredInputChange.length;i++) {
					state.registeredInputChange[i](newValue);
				}

				if(angular.isFunction($scope.dfAutocompleteConfiguration.autocompleteProvider)) {
					var res = $scope.dfAutocompleteConfiguration.autocompleteProvider(newValue);
					
					if(res === false) {
						return;
					}
					
					if(angular.isArray(res)) {
						var deferred = $q.defer();
						deferred.resolve(res);
						res = deferred.promise;
					}
				
					res.then(function(result) {
						state.showAutocompleteList = showAutocompleteList(newValue, result);
						for(var i = 0;i<state.registeredAutocompleteChange.length;i++) {
							state.registeredAutocompleteChange[i](result);
						}
					});
				};
			};
			
			$scope.focusOnInput = function() {
				handleUserInput($scope.ngModel.userInput);
			};
			
			$scope.$watch('ngModel.userInput', function(newValue, oldValue) {
				if(!angular.equals(newValue, oldValue)) {
					handleUserInput(newValue);
				}
			});
			
			
			$scope.menuSelection = function(param) {
			};
			
			
			
		},
		link: function($scope,element,attrs, controller, transcludeFn) {
		
			var state = $scope.dfAutocompleteInternalState;
			
			var updateSizes = function() {
				var chevronElem = element[0].querySelector('.df-autocomplete-chevron');
				var menuWidth = chevronElem ? chevronElem.offsetWidth : 0;
				state.inputMaxWidth = (element[0].querySelector('.df-autocomplete-box').offsetWidth - menuWidth);
				element[0].querySelector('.df-autocomplete-input > div').style.width = state.inputMaxWidth +'px';
				if(chevronElem) {
					element[0].querySelector('.df-autocomplete-controls').style.width = menuWidth+'px';
				}
				
				updateInputElementSize();
			};
			
			//
			$scope.$watch('dfAutocompleteInternalState.menuPresent', updateSizes);
			$scope.$watch(function(){return element[0].querySelector('.df-autocomplete-box').offsetWidth}, updateSizes);
			//
			
			var $inputRef = function() {
				return element[0].querySelectorAll('.df-autocomplete-input input[data-input-index=\''+$scope.dfAutocompleteInternalState.inputPosition+'\']')[0];
			};
			
			//
			//TODO handle size for input field -> set size for 
			var inputContainer = element[0].querySelector('.df-autocomplete-input > div');
			//
						
			$scope.focusToInputField = function(event) {
				$timeout(function() {
					var $input = $inputRef();
					$input.focus();
					if ($input.value && event && event.target != $input) {
						$input.setSelectionRange($input.value.length, $input.value.length);
					}
				},0);
				$scope.dfAutocompleteInternalState.mustFocusOnInput = false;
			};
			
			var updateInputElementSize = function() {
				
				var $span = element[0].querySelectorAll('.df-autocomplete-input-size')[0];
				//
				var $input = $inputRef();
				var $inputComputedStyle = getComputedStyle($input);
				$span.style.fontSize = $inputComputedStyle.fontSize;
				$span.style.fontFamily = $inputComputedStyle.fontFamily;
				$span.style.fontWeight = $inputComputedStyle.fontWeight;
				$span.style.letterSpacing = $inputComputedStyle.letterSpacing;	
				//
				
				
			
			
				var inputValue = $input.value;
				if($input.value != null) {
					inputValue = $input.value.replace(/\s/g, '&nbsp;');
				}
				$span.innerText = inputValue + 'M'; //'M' -> add a little bit of additional space
				var spanWidth = element[0].querySelectorAll('.df-autocomplete-input-size')[0].clientWidth;
				
				$input.style.maxWidth = (state.inputMaxWidth - 5) + 'px';
				if(spanWidth < state.inputMaxWidth) {
					$input.style.width = spanWidth < 5 ? '4px' : spanWidth+'px';
				}
				
				
				
			};
			
			var inputWatchers = [];
			

			$scope.initInput = function() {
				$timeout(function() {
				
					var $input = $inputRef();
					$input.addEventListener('keypress', updateInputElementSize);
					
					//unregister and remove all previous watchers
					angular.forEach(inputWatchers, function(v) {v();});
					inputWatchers.length = 0;
					
					inputWatchers.push($scope.$watch(function() {return $input.value;}, updateInputElementSize));
					if(state.mustFocusOnInput) {
						$scope.focusToInputField();
					}
				}, 0);
			};
			
			$scope.handleKeyDown = function(event) {
			
			
				/* handle esc key -> hide the autocomplete menu */
				if(event.keyCode === 27 /* esc */) {
					//hide the autocomplete
					state.showAutocompleteList = false;
					event.preventDefault();
					return;
				}
				
				
				var $input = $inputRef();

				/* handle the left/right/home/end key for moving the input field between tags*/
				if(($input.value === "" || $input.value == null) && (event.keyCode === 37 /* left */ || event.keyCode === 39 /* right */ || event.keyCode == 36 /*home*/ || event.keyCode == 35 /* end*/)) {
					
					var newPos;
					if(event.keyCode >= 37) {
						newPos = state.inputPosition + (event.keyCode === 37 ? -1 : 1);
					} else {
						newPos = event.keyCode === 36 ? -($scope.ngModel.tags.length) : 0;
					}
					
					if(newPos >= -($scope.ngModel.tags.length) && newPos <= 0)  {
						state.mustFocusOnInput = true;
						state.inputPosition = newPos;
					}
					
					return;
				}
				
				//remove tags
				if($scope.ngModel.tags && ($input.value === "" || $input.value == null) && event.keyCode === 8 /* backspace */  && $scope.ngModel.tags.length > 0) {

					var toRemove = $scope.ngModel.tags[$scope.ngModel.tags.length + state.inputPosition - 1];
					
					if(toRemove) {
						if($scope.dfAutocompleteConfiguration.restoreOnBackspace) {
							$scope.ngModel.userInput = ($scope.dfAutocompleteConfiguration.textInputRepresentation || angular.identity)(toRemove);
						}
						$scope.removeTag(toRemove);
					}
					event.preventDefault();
					
					return;
				}
			
				/**/
				for(var i = 0;i<state.registeredKeyDown.length;i++) {
					state.registeredKeyDown[i](event);
				}
			};

			//-------------
			
			element.bind('click', function() {
				element.addClass('df-autocomplete-focused');
			});
			
			
			element[0].addEventListener('focus', function() {
				element.addClass('df-autocomplete-focused');
			}, true);
			
			function handleClickOutsideAutocomplete(ev) {
				
				var elemIsDescendant = isDescendant(element[0], ev.target);
				var hasClickedOnTag = ev!=null && ev.target !=null && ev.target.parentNode != null && ev.target.parentNode.className === 'lvg-remove-tag';
				
				if(!elemIsDescendant && !hasClickedOnTag) {
					element.removeClass('df-autocomplete-focused');
				}
				
				if(state.showAutocompleteList && !elemIsDescendant) {
					$scope.$apply(function() {state.showAutocompleteList = false;});
				}
				
				if(state.showMenu && !elemIsDescendant) {
					$scope.$apply(function() {state.showMenu = false;});
				}
			};
		
			//hide the auto complete list/menu if clicked outside of it and the directive.
			$window.document.addEventListener('click', handleClickOutsideAutocomplete);
			$scope.$on('$destroy', function() {
				window.document.removeEventListener('click', handleClickOutsideAutocomplete);
			});
			
			
			
			//-------------
		}
	};
  }])
  
  .directive('dfAutocompleteMenu', [function() {
	return {
		restrict: 'E',
		transclude: true,
		require: '^dfAutocomplete',
		template: '<div class="df-autocomplete-menu"></div>',
		link: function($scope, element, attrs, dfAutocomplete, transcludeFn) {
			
			element.children()[0].style.width=element.parent().parent()[0].clientWidth+'px';
			
			var $autocompleteScope = $scope.$$prevSibling;
			
			transcludeFn($autocompleteScope, function(clone, scope) {
				element.find('div').append(clone);
			});
			$autocompleteScope.dfAutocompleteInternalState.menuPresent = true;
		}
	}
  }])
  .directive('dfAutocompleteList', ['$timeout', function($timeout) {
	return {
		restrict: 'E',
		transclude: true,
		require: '^dfAutocomplete',
		template: '<div class="df-autocomplete-list"></div>',
		link: function($scope, element, attrs, dfAutocomplete, transcludeFn) {
		
			var $autocompleteScope = $scope.$$prevSibling;
			var state = $autocompleteScope.dfAutocompleteInternalState;
			
			var listCount = function() {
				return (state.autocompleteResult ? state.autocompleteResult.length : 0);
			};
		
			element.children()[0].style.width=element.parent().parent()[0].clientWidth+'px';
			
			state.registeredInputChange.push(function(inputChange) {
				$timeout(function() {
					state.autocompleteSelectionIndex = 0;
				}, 0);
			});
			
			state.registeredAutocompleteChange.push(function(autocompleteResult) {
				$timeout(function() {
					state.autocompleteSelectionIndex = 0;
					state.autocompleteResult = autocompleteResult;
				}, 0);
			});
		
			
			state.registeredKeyDown.push(function(event) {
			
				if(!state.showAutocompleteList) {
					return;
				}
			
				if(event.keyCode === 38 /* up */ || event.keyCode === 40 /* down */) {
					$timeout(function() {
						var cnt = listCount();
						var r = state.autocompleteSelectionIndex + (event.keyCode == 38 ? -1 : 1);
						//wrap the index...
						if(r < 0) {
							r = cnt -1;
						} else if(r >= cnt) {
							r = 0;
						}
						//
						state.autocompleteSelectionIndex = r;
					}, 0);
					event.preventDefault();
				} else if(event.keyCode === 13 /* enter */ || event.keyCode === 9 /* tab */ ) {
					event.preventDefault();
					//click over the element with the class .df-autocomplete-selected
					$timeout(function() {element[0].querySelector('.df-autocomplete-selected').click()}, 0);
				}
			});
			

			transcludeFn($autocompleteScope, function(clone, scope) {
				element.find('div').append(clone);
			});
		}
	}
  }])
  
  .directive('dfAutocompleteTagRender', ['$compile', function($compile) {
	return {
		restrict:'A',
		link: function($scope, element, attrs) {
			element.empty().append($compile('<span>'+$scope.dfAutocompleteConfiguration.tagTemplate+'</span>')($scope));
		}
	};
  }])
  
  ;  
})();