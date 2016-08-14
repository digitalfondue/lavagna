(function () {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2', {
		bindings: {
			readOnly: '@', /* true | false (default) */
			view: '@', /* list | board | search */
			hideSelect: '@', /* "true" | ... */
			searchType: '@', /* globalSearch | projectSearch (default) */
			cardRef:'&',
			userRef:'&',
			projectMetadataRef: '&',
			selectedRef:'&'
		},
		controller: ['$element', '$scope', '$state', '$location', '$filter', 'User', 'Card', 'LvgIcon', '$rootScope', 'UserCache', 'CardCache', CardFragmentV2Controller]
	});
	
	


	function CardFragmentV2Controller($element, $scope, $state, $location, $filter, User, Card, LvgIcon, $rootScope, UserCache, CardCache) {
		var ctrl = this;
		
		
		var container = createElem('div');
		container.appendChild(createElem('lvg-card-fragment-v2-head'));
		container.appendChild(createElem('lvg-card-fragment-v2-data-info'));
		
		var $container = angular.element(container);

		ctrl.card = ctrl.cardRef();
		//
		ctrl.boardShortName = ctrl.card.boardShortName,
		ctrl.projectShortName = ctrl.card.projectShortName,
		//
		ctrl.user = ctrl.userRef();
		ctrl.projectMetadata = ctrl.projectMetadataRef();
		//
		ctrl.selected = ctrl.selectedRef();

		ctrl.readOnly = ctrl.readOnly != undefined;
        ctrl.listView = ctrl.view != undefined && ctrl.view == 'list';
        ctrl.boardView = ctrl.view != undefined && ctrl.view == 'board';
        ctrl.searchView = ctrl.view != undefined && ctrl.view == 'search';

        ctrl.shortCardName = ctrl.card.boardShortName + ' - ' + ctrl.card.sequence;

        if(ctrl.hideSelect === undefined) {
        	ctrl.hideSelect = false;
        }

        //
        ctrl.isSelfWatching = Card.isWatchedByUser(ctrl.card.labels, ctrl.user.id);
        ctrl.isAssignedToCard = Card.isAssignedToUser(ctrl.card.labels, ctrl.user.id);
        //
        
        
        var headElem = $container.find('lvg-card-fragment-v2-head');
        var headCtrl = new lvgCardFragmentV2HeadCtrl(headElem, $scope, $state, $location, $filter, User);
        headCtrl.lvgCardFragmentV2 = ctrl;
        headCtrl.$postLink();
        
        
        var dataInfoElem = $container.find('lvg-card-fragment-v2-data-info');
        var dataInfoCtrl = new lvgCardFragmentV2DataInfoCtrl($filter, dataInfoElem, LvgIcon, $state, $rootScope, UserCache, CardCache);
        dataInfoCtrl.lvgCardFragmentV2 = ctrl;
        dataInfoCtrl.$postLink();
        
        
        if(!ctrl.listView && ctrl.isSelfWatching) {
        	container.className += ' lavagna-is-watching';
		}
        
        if(ctrl.boardView) {
        	container.className += ' lavagna-board-panel';
        }
		
		$element[0].appendChild(container);
        
        ctrl.$onDestroy = function() {
        	dataInfoCtrl.$onDestroy();
        }
        
        
	}
	
	
	function createElem(name) {
		return window.document.createElement(name);
	}

	function createText(value) {
		return window.document.createTextNode(value);
	}
	
	
	
	//--------------------
	function lvgCardFragmentV2HeadCtrl($element, $scope, $state, $location, $filter, User) {
		var ctrl = this;
		var domElement = $element[0];

		ctrl.$postLink = function lvgCardFragmentV2HeadCtrlPostLink() {
			var parent = ctrl.lvgCardFragmentV2;

			var baseDiv = createElem('div');

			if (parent.boardView && !parent.readOnly) {

				if(parent.hideSelect !== 'true' && User.checkPermissionInstant(parent.user, 'MANAGE_LABEL_VALUE', parent.card.projectShortName)) {
					baseDiv.appendChild(checkbox());
				}

				var a = createLink('board.card', parent.projectShortName, parent.boardShortName, parent.card.sequence, true);
				baseDiv.appendChild(a);
				//<div lvg-has-at-least-one-permission="MOVE_CARD,MANAGE_LABEL_VALUE">
                //  <div class="lavagna-card-caret-container lvg-not-sortable-card" ng-click="openCardMenu($ctrl.card, $ctrl.projectMetadataRef())"><span class="fa fa-chevron-down"></span></div>
				//</div>
			} else if (parent.boardView && parent.readOnly) {
				baseDiv.appendChild(createText(parent.shortCardName));
				angular.element(baseDiv).addClass('fake-link');
			} else if (parent.listView) {
				var a = createLink('board.card', parent.projectShortName, parent.boardShortName, parent.card.sequence, false);
				baseDiv.appendChild(a);
				baseDiv.appendChild(lastUpdateTime(lastUpdateTime));
			} else if (parent.searchView) {

				if(User.checkPermissionInstant(parent.user, 'MANAGE_LABEL_VALUE', parent.card.projectShortName)) {
					baseDiv.appendChild(checkbox());
				}

				var route = parent.searchType == 'globalSearch' ? 'globalSearch.card' : 'projectSearch.card';
				var a = createLink(route, parent.projectShortName, parent.boardShortName, parent.card.sequence, true);
				baseDiv.appendChild(a);
				baseDiv.appendChild(lastUpdateTime(parent.card.lastUpdateTime));
			}
			domElement.appendChild(baseDiv);
			domElement.appendChild(createText(parent.card.name))
		}

		function lastUpdateTime(lastUpdateTime) {
			var e = angular.element(createElem('div')).addClass('card-home-date')[0];
			e.textContent = $filter('dateIncremental')(lastUpdateTime);
			return e;
		}

		function checkbox() {
			var c = createElem("input");
			angular.element(c).attr('type', 'checkbox');
			var parent = ctrl.lvgCardFragmentV2;


			var selected = parent.selected;
			var card = parent.card;

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
				c.addEventListener('click', function() {
					$scope.$applyAsync(function() {
						selected[card.columnId] = selected[card.columnId] || {};
						selected[card.columnId][card.id] = !selected[card.columnId][card.id]
					});
    			});
			} else {
				c.addEventListener('click', function() {
					$scope.$applyAsync(function() {
						selected[card.projectShortName] = selected[card.projectShortName] || {};
						selected[card.projectShortName][card.id] = !selected[card.projectShortName][card.id]
					});
				});
			}

			return c;
		}

		function createLink(targetState, projectName, boardShortName, sequenceNumber, isDynamicLink) {
			var a = createElem("a");
			a.textContent = boardShortName + ' - ' + sequenceNumber;
			var $a = angular.element(a);
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
		
		
		//--------------------------------------
		
	}
	
	
	function appendIfNotNull(parent, child) {
		if(child) {
			parent.appendChild(child);
		}
	}

	function updateCardClass(card, element) {
		if (card.columnDefinition != 'CLOSED') {
			element.removeClass('lavagna-closed-card');
		} else {
			element.addClass('lavagna-closed-card');
		}
	}

	function addDueDateClasses(li, isTomorrow, isNow, isPast) {
    	if(isTomorrow) {
    		li.className += ' lvg-due-date-tomorrow';
    	}
    	if(isNow) {
    		li.className = ' lvg-due-date-now';
    	}
    	if(isPast) {
    		li.className = 'lvg-due-date-past';
    	}
    }
	
	
	function lvgCardFragmentV2DataInfoCtrl($filter, $element, LvgIcon, $state, $rootScope, UserCache, CardCache) {
		var ctrl = this;

		var card;
		var projectMetadata;
		var notClosed;

		var listeners = [];



		ctrl.$postLink = function lvgCardFragmentV2DataInfoCtrlPostLink() {
			card = ctrl.lvgCardFragmentV2.card;
			projectMetadata = ctrl.lvgCardFragmentV2.projectMetadata;
	        notClosed = card.columnDefinition !== 'CLOSED';

			// metadata
			var liComment = handleComment();
			var liActionList = handleActionList();
			var liFiles = handleFiles();
			var liDueDate = handleDueDate();
			var liMilestone = handleMilestone();
			//

			if(liComment || liActionList || liFiles || liDueDate || liMilestone) {
				var divWrapper = createElem('div');
				divWrapper.className = 'card-data';
				var ul = createElem('ul');
				ul.className = 'data-info';
				divWrapper.appendChild(ul);
				$element[0].appendChild(divWrapper);
				appendIfNotNull(ul, liComment);
				appendIfNotNull(ul, liActionList);
				appendIfNotNull(ul, liFiles);
				appendIfNotNull(ul, liDueDate);
				appendIfNotNull(ul, liMilestone);
			}

			handleAssigned();

			handleLabels();
		}

		ctrl.$onDestroy = function lvgCardFragmentV2DataInfoCtrlOnDestroy() {
			for(var i = 0; i < listeners.length; i++) {
				listeners[i]();
			}
		}

        //

        function hasCountGreaterThanZero(name) {
        	return getCountOrZero(name) > 0;
        }

        function getCountOrZero(name) {
        	return (card.counts && card.counts[name]) ? card.counts[name].count : 0;
        }


        function filterSystemLabelByName(labelName) {
        	var res = [];
        	for(var i = 0; i < card.labels.length;i++) {
        		var label = card.labels[i]; 
        		if(label.labelDomain === 'SYSTEM' && label.labelName === labelName) {
        			res.push(label);
        		}
        	}
        	return res;
        }
        
        function filterUserLabels() {
        	var res = [];
        	for(var i = 0; i < card.labels.length;i++) {
        		var label = card.labels[i]; 
        		if(label.labelDomain === 'USER') {
        			res.push(label);
        		}
        	}
        	return res;
        }


        function handleComment() {
        	if(!(card.counts && card.counts['COMMENT'] && card.counts['COMMENT'].count > 0)) {
        		return null;
        	}

        	var li = createElem('li');

    		appendIconAndText(li, LvgIcon.comment, card.counts['COMMENT'].count);
    		return li;
        }

        function handleActionList() {

        	var hasActionListWithItems = hasCountGreaterThanZero('ACTION_CHECKED') || hasCountGreaterThanZero('ACTION_UNCHECKED');

        	if(!hasActionListWithItems) {
        		return null;
        	}

        	var checkedCount = getCountOrZero('ACTION_CHECKED');
        	var uncheckedCount = getCountOrZero('ACTION_UNCHECKED');
        	var actionItemsSummary = (checkedCount) + '/' + (checkedCount + uncheckedCount);

        	var li = createElem('li');

    		var counts = card.counts;

    		if(checkedCount > 0 && uncheckedCount == 0) {
    			li.className += ' lvg-action-full';
    		}
    		if(card.columnDefinition == 'CLOSED' && uncheckedCount > 0) {
    			li.className += ' lvg-action-not-done';
    		}

    		appendIconAndText(li, LvgIcon.list, actionItemsSummary);
        	return li;
        }




        function handleFiles() {
        	var hasFiles = hasCountGreaterThanZero('FILE');
        	if(!hasFiles) {
        		return null;
        	}

        	var filesCount = hasFiles ? getCountOrZero('FILE') : '';
        	var li = createElem('li');

    		appendIconAndText(li, LvgIcon.file, filesCount);

    		return li;
        }

        function handleDueDate() {
        	var dueDateLabels = filterSystemLabelByName('DUE_DATE');
        	var hasDueDateLabel = dueDateLabels.length == 1;
        	if(!hasDueDateLabel) {
        		return null;
        	}

        	var dueDateLabel = dueDateLabels[0];
        	var daysDiff = $filter('daysDiff')(dueDateLabel.labelValueTimestamp);

        	var isTomorrow = notClosed  && daysDiff == -1;
        	var isNow = notClosed && daysDiff == 0;
        	var isPast = notClosed && daysDiff > 0;


        	var li = createElem('li');

    		addDueDateClasses(li, isTomorrow, isNow, isPast);

    		var value = dueDateLabel.value || dueDateLabel;

    		appendIconAndText(li, LvgIcon.clock, $filter('date')(value.valueTimestamp, 'dd.MM.yyyy'));
    		return li;
        }

        function handleMilestone() {

        	var milestoneLabels = filterSystemLabelByName('MILESTONE');
            var hasMilestoneLabel = milestoneLabels.length === 1;

        	if(!hasMilestoneLabel) {
        		return null;
        	}

        	if(!projectMetadata) {
        		return null;
        	}

        	var milestoneLabel = '';

        	milestoneLabel = milestoneLabels[0];

        	var releaseDateStr = undefined;
        	var metadata = projectMetadata;
        	if(metadata && metadata.labelListValues && milestoneLabel && milestoneLabel.labelValueList && metadata.labelListValues[milestoneLabel.labelValueList].metadata) {
        		releaseDateStr = metadata.labelListValues[milestoneLabel.labelValueList].metadata.releaseDate;
        	}

        	var li = createElem('li');

        	if(releaseDateStr) {
        		var releaseDate = moment(releaseDateStr, "DD.MM.YYYY");//FIXME format server side
        		var daysDiff = $filter('daysDiff')(releaseDate);

        		var isTomorrow = (notClosed  && daysDiff == -1);
            	var isNow = (notClosed && daysDiff == 0);
            	var isPast = (notClosed && daysDiff > 0);

        		addDueDateClasses(li, isTomorrow, isNow, isPast);
        	}

    		appendIconAndText(li, LvgIcon.milestone, projectMetadata.labelListValues[milestoneLabel.labelValueList].value);
    		return li;
        }

        //------------
        function handleAssigned() {
        	var assignedLabels = filterSystemLabelByName('ASSIGNED');
        	if(assignedLabels.length === 0) {
        		return;
        	}

        	var divWrapper = angular.element(createElem('div')).addClass('card-info')[0];
        	var ul = angular.element(createElem('ul')).addClass('assigned-users')[0];
        	divWrapper.appendChild(ul);
        	for(var i = 0; i < assignedLabels.length; i++) {
        		var userId = assignedLabels[i].value.valueUser;
        		var a = handleUser(userId);
        		var li = angular.element(createElem('li')).addClass('assigned-user')[0];
        		li.appendChild(a);
        		ul.appendChild(li);
        	}
        	$element[0].appendChild(divWrapper);
        }
        //------------
        var labelBackgroundClass = $filter('labelBackgroundClass');
        var labelBackground = $filter('labelBackground');
        function handleLabels() {

        	if(!projectMetadata) {
        		return null;
        	}

        	var userCreatedLabels = filterUserLabels();
        	if(userCreatedLabels.length === 0) {
        		return;
        	}


        	var divWrapper = angular.element(createElem('div')).addClass('card-labels')[0];
        	var ul = angular.element(createElem('ul')).addClass('labels')[0];
        	divWrapper.appendChild(ul);
        	for(var i = 0; i < userCreatedLabels.length; i++) {
        		var value = userCreatedLabels[i];
        		var bg = labelBackground(projectMetadata.labels[value.labelId].color);


        		//
        		var addSeparator = (value.labelValueType || value.type) !== 'NULL';
            	var name = projectMetadata.labels[value.labelId].name;
        		var nameAndSeparator = name + (addSeparator ? ': ' : '' );
        		var userOrCardLink = null;
        		switch(value.labelValueType) {
        		case 'NULL':
        			break;
        		case 'STRING':
        			nameAndSeparator += ' ' + value.value.valueString;
        			break;
        		case 'INT':
        			nameAndSeparator += ' ' + value.value.valueInt;
        			break;
        		case 'LIST':
        			nameAndSeparator += ' ' + projectMetadata.labelListValues[value.value.valueList].value
        			break;
        		case 'TIMESTAMP':
        			nameAndSeparator += ' ' + $filter('date')(value.value.valueTimestamp, 'dd.MM.yyyy');
        			break;
        		case 'USER':
        			userOrCardLink = handleUser(value.value.valueUser);
        			break;
        		case 'CARD':
        			userOrCardLink = handleCard(value.value.valueCard);
        			break;
        		}

        		//
        		var li = angular.element(createElem('li'))
        			.addClass('lavagna-label')
        			.addClass('lavagna-label-no-controls')
        			.addClass(labelBackgroundClass(projectMetadata.labels[value.labelId].color))
        			.attr('style', 'background-color:' + bg['background-color'])
        			.text(nameAndSeparator)[0];
        		if(userOrCardLink) {
        			li.appendChild(userOrCardLink);
        		}

        		ul.appendChild(li);
        	}
        	$element[0].appendChild(divWrapper);
        }

        //------------

        function appendIconAndText(li, iconFactory, text) {
        	var icon = createElem('md-icon');        	
        	icon.appendChild(iconFactory());
    		li.appendChild(icon);
    		li.appendChild(createText(' ' + text));
        }

        //-----------

		function handleUser(userId) {
			var a = createElem('a');
			UserCache.user(userId).then(function (user) {
				var element = angular.element(a);
				element.attr('href', $state.href('user.dashboard', {provider: user.provider, username: user.username}));
				element.text($filter('formatUser')(user));
				if (!user.enabled) {
					element.addClass('user-disabled');
				}
			});
			return a;
		}


		function handleCard(cardId) {

			var a = createElem('a');

			CardCache.card(cardId).then(function (card) {
				var element = angular.element(a);

				a.textContent = card.boardShortName + '-' + card.sequence;
				element.attr('href', $state.href('board.card', {projectName: card.projectShortName, shortName: card.boardShortName, seqNr: card.sequence}));

				updateCardClass(card, element);

				var toDismiss = $rootScope.$on('refreshCardCache-' + cardId, function () {
					CardCache.card(cardId).then(function (card) {
						updateCardClass(card, element);
					});
				});
				listeners.push(toDismiss);
			});

			return a;
		}
	}
	
	

})();
