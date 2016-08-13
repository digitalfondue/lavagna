(function() {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Head', {
		require : {
			lvgCardFragmentV2 : '^lvgCardFragmentV2'
		},
		controller : [ '$element', '$scope', '$window', '$state', '$location', '$filter', 'User', lvgCardFragmentV2HeadCtrl ]
	});

	function lvgCardFragmentV2HeadCtrl($element, $scope, $window, $state, $location, $filter, User) {
		const ctrl = this;
		const domElement = $element[0];
		
		ctrl.$postLink = function lvgCardFragmentV2HeadCtrlPostLink() {
			const parent = ctrl.lvgCardFragmentV2;
			
			const baseDiv = createElem('div');

			if (parent.boardView && !parent.readOnly) {
				
				//<input lvg-if-instant="::($ctrl.hideSelect !== 'true')" type="checkbox"  lvg-has-permission="MANAGE_LABEL_VALUE" class="lavagna-board-card-checkbox" lvg-card-fragment-checkbox></input>
				if(parent.hideSelect !== 'true' && User.checkPermissionInstant(parent.user, 'MANAGE_LABEL_VALUE', parent.card.projectShortName)) {
					baseDiv.appendChild(checkbox());
				}
				
				const a = createLink('board.card', parent.projectShortName, parent.boardShortName, parent.card.sequence, true);
				baseDiv.appendChild(a);
				//<div lvg-has-at-least-one-permission="MOVE_CARD,MANAGE_LABEL_VALUE">
                //  <div class="lavagna-card-caret-container lvg-not-sortable-card" ng-click="openCardMenu($ctrl.card, $ctrl.projectMetadataRef())"><span class="fa fa-chevron-down"></span></div>
				//</div>
			} else if (parent.boardView && parent.readOnly) {
				baseDiv.appendChild(createText(parent.shortCardName));
				angular.element(baseDiv).addClass('fake-link');
			} else if (parent.listView) {
				const a = createLink('board.card', parent.projectShortName, parent.boardShortName, parent.card.sequence, false);
				baseDiv.appendChild(a);
				baseDiv.appendChild(lastUpdateTime(lastUpdateTime));
			} else if (parent.searchView) {
				
				//<input type="checkbox" lvg-has-permission="MANAGE_LABEL_VALUE" data-with-project="{{::$ctrl.card.projectShortName}}" lvg-card-fragment-checkbox>
				if(User.checkPermissionInstant(parent.user, 'MANAGE_LABEL_VALUE', parent.card.projectShortName)) {
					baseDiv.appendChild(checkbox());
				}
				
				const route = parent.searchType == 'globalSearch' ? 'globalSearch.card' : 'projectSearch.card';
				const a = createLink(route, parent.projectShortName, parent.boardShortName, parent.card.sequence, true);
				baseDiv.appendChild(a);
				baseDiv.appendChild(lastUpdateTime(parent.card.lastUpdateTime));
			}
			domElement.appendChild(baseDiv);
			domElement.appendChild(createText(parent.card.name))
		}
		
		function lastUpdateTime(lastUpdateTime) {
			const e = angular.element(createElem('div')).addClass('card-home-date')[0];
			e.textContent = $filter('dateIncremental')(lastUpdateTime);
			return e;
		}
		
		function checkbox() {
			const c = createElem("input");
			const $c = angular.element(c).attr('type', 'checkbox');
			const parent = ctrl.lvgCardFragmentV2;
			
			
			const selected = parent.selected;
			const card = parent.card;
			
			function isSelected() {
				if(parent.boardView) {
					return (selected[card.columnId] && (selected[card.columnId][card.id] === true));
				} else {
					return (selected[card.projectShortName] && (selected[card.projectShortName][card.id] === true));
				}
			};
			
			function updateCheckbox() {
				c.checked = isSelected();
			};
			
			updateCheckbox();
			
			$scope.$on('updatecheckbox', updateCheckbox);
			
			if(parent.boardView) {
				$c.on('click', function() {
					$scope.$applyAsync(function() {
						selected[card.columnId] = selected[card.columnId] || {};
						selected[card.columnId][card.id] = !selected[card.columnId][card.id]
					});
    			});
			} else {
				$c.on('click', function() {
					$scope.$applyAsync(function() {
						selected[card.projectShortName] = selected[card.projectShortName] || {};
						selected[card.projectShortName][card.id] = !selected[card.projectShortName][card.id]
					});
				});
			}
			
			return c;
		}
		
		
		function createElem(name) {
			return $window.document.createElement(name);
		}
		
		function createText(value) {
			return $window.document.createTextNode(value);
		}
		
		function createLink(targetState, projectName, boardShortName, sequenceNumber, isDynamicLink) {
			const a = createElem("a");
			a.textContent = boardShortName + ' - ' + sequenceNumber;
			const $a = angular.element(a);
			$a.attr('href', updateUrl($location.search().q, $location.search().page, targetState, projectName, boardShortName, sequenceNumber));
			if(isDynamicLink) {
				$scope.$on('updatedQueryOrPage', function(ev, searchFilter) {
					$a.attr('href', updateUrl(searchFilter.location ? searchFilter.location.q : null, $location.search().page, targetState, projectName, boardShortName, sequenceNumber))
				});
			}
			return a;
		}
		
		function updateUrl(q, page, targetState, projectName, boardShortName, sequenceNumber) {
			return $state.href(targetState, {
				projectName: projectName, 
				shortName: boardShortName, 
				seqNr: sequenceNumber, 
				q:  q, 
				page: page});
		}
	}

})();