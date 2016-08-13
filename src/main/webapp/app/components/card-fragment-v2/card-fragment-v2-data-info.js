(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentV2DataInfo', {
		require : {
			lvgCardFragmentV2 : '^lvgCardFragmentV2'
		},
        controller: ['$filter', '$element', '$window', '$mdIcon', '$state', '$rootScope', 'UserCache', 'CardCache', lvgCardFragmentV2DataInfoCtrl]
	})
	
	function lvgCardFragmentV2DataInfoCtrl($filter, $element, $window, $mdIcon, $state, $rootScope, UserCache, CardCache) {
		const ctrl = this;
		
		var card;
		var projectMetadata;
		var notClosed;
		
		var listeners = [];
		

		
		ctrl.$postLink = function lvgCardFragmentV2DataInfoCtrlPostLink() {
			
			
			card = ctrl.lvgCardFragmentV2.card;
			projectMetadata = ctrl.lvgCardFragmentV2.projectMetadata;
	        notClosed = card.columnDefinition !== 'CLOSED';
			
			// metadata
			const liComment = handleComment();
			const liActionList = handleActionList();
			const liFiles = handleFiles();
			const liDueDate = handleDueDate();
			const liMilestone = handleMilestone();
			//
			
			if(liComment || liActionList || liFiles || liDueDate || liMilestone) {
				const divWrapper = angular.element(createElem('div')).addClass('card-data')[0];
				const ul = angular.element(createElem('ul')).addClass('data-info')[0];
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
        	return $filter('filter')(card.labels, {labelDomain:'SYSTEM', labelName: labelName});
        }
        
        
        function handleComment() {
        	if(!(card.counts && card.counts['COMMENT'] && card.counts['COMMENT'].count > 0)) {
        		return null;
        	}
        	
        	const li = createElem('li');
    		
    		appendIconAndText(li, 'comment', card.counts['COMMENT'].count);
    		return li;
        }
        
        function handleActionList() {
        	
        	const hasActionListWithItems = hasCountGreaterThanZero('ACTION_CHECKED') || hasCountGreaterThanZero('ACTION_UNCHECKED');
        	
        	if(!hasActionListWithItems) {
        		return null;
        	}
        	
        	const checkedCount = getCountOrZero('ACTION_CHECKED');
        	const uncheckedCount = getCountOrZero('ACTION_UNCHECKED');
        	const actionItemsSummary = (checkedCount) + '/' + (checkedCount + uncheckedCount);
        	
        	const li = createElem('li');
    		const $li = angular.element(li);
    		
    		const counts = card.counts;
    		
    		if(checkedCount > 0 && uncheckedCount == 0) {
    			$li.addClass('lvg-action-full');
    		}
    		if(card.columnDefinition == 'CLOSED' && uncheckedCount > 0) {
    			$li.addClass('lvg-action-not-done');
    		}
    		
    		appendIconAndText(li, 'list', actionItemsSummary);
        	return li;
        }
        
        
        
        
        function handleFiles() {
        	const hasFiles = hasCountGreaterThanZero('FILE');
        	if(!hasFiles) {
        		return null;
        	}
        	
        	const filesCount = hasFiles ? getCountOrZero('FILE') : '';
        	const li = createElem('li');
    		
    		appendIconAndText(li, 'file', filesCount);
        	
    		return li;
        }
        
        function handleDueDate() {
        	const dueDateLabels = filterSystemLabelByName('DUE_DATE');
        	const hasDueDateLabel = dueDateLabels.length == 1;
        	if(!hasDueDateLabel) {
        		return null;
        	}
        	
        	const dueDateLabel = dueDateLabels[0];
        	const daysDiff = $filter('daysDiff')(dueDateLabel.labelValueTimestamp);
        	
        	const isTomorrow = notClosed  && daysDiff == -1;
        	const isNow = notClosed && daysDiff == 0;
        	const isPast = notClosed && daysDiff > 0;

        	
        	const li = createElem('li');
    		const $li = angular.element(li);
    		
    		addDueDateClasses($li, isTomorrow, isNow, isPast);
    		
    		const value = dueDateLabel.value || dueDateLabel;
    		
    		appendIconAndText(li, 'clock', $filter('date')(value.valueTimestamp, 'dd.MM.yyyy'));
    		return li;
        }
        
        function handleMilestone() {
        	
        	const milestoneLabels = filterSystemLabelByName('MILESTONE');
            const hasMilestoneLabel = milestoneLabels.length === 1;
        	
        	if(!hasMilestoneLabel) {
        		return null;
        	}
        	
        	if(!projectMetadata) {
        		return null;
        	}
        	
        	var milestoneLabel = '';

        	milestoneLabel = milestoneLabels[0];
        	
        	var releaseDateStr = undefined;
        	const metadata = projectMetadata;
        	if(metadata && metadata.labelListValues && milestoneLabel && milestoneLabel.labelValueList && metadata.labelListValues[milestoneLabel.labelValueList].metadata) {        	
        		releaseDateStr = metadata.labelListValues[milestoneLabel.labelValueList].metadata.releaseDate;
        	}
        	
        	const li = createElem('li');
    		const $li = angular.element(li);
        	
        	if(releaseDateStr) {
        		const releaseDate = moment(releaseDateStr, "DD.MM.YYYY");//FIXME format server side
        		const daysDiff = $filter('daysDiff')(releaseDate);
        		
        		const isTomorrow = (notClosed  && daysDiff == -1);
            	const isNow = (notClosed && daysDiff == 0);
            	const isPast = (notClosed && daysDiff > 0);
        		
        		addDueDateClasses($li, isTomorrow, isNow, isPast);
        	}

    		appendIconAndText(li, 'milestone', projectMetadata.labelListValues[milestoneLabel.labelValueList].value);
    		return li;
        }
        
        //------------
        function handleAssigned() {
        	const assignedLabels = filterSystemLabelByName('ASSIGNED');
        	if(assignedLabels.length === 0) {
        		return;
        	}
        	
        	const divWrapper = angular.element(createElem('div')).addClass('card-info')[0];
        	const ul = angular.element(createElem('ul')).addClass('assigned-users')[0];
        	divWrapper.appendChild(ul);
        	for(var i = 0; i < assignedLabels.length; i++) {
        		const userId = assignedLabels[i].value.valueUser;
        		const a = handleUser(userId);
        		const li = angular.element(createElem('li')).addClass('assigned-user')[0];
        		li.appendChild(a);
        		ul.appendChild(li);
        	}
        	$element[0].appendChild(divWrapper);
        }
        //------------
        const labelBackgroundClass = $filter('labelBackgroundClass');
        const labelBackground = $filter('labelBackground');
        function handleLabels() {
        	
        	if(!projectMetadata) {
        		return null;
        	}
        	
        	const userCreatedLabels = $filter('filter')(card.labels, {labelDomain:'USER'});
        	if(userCreatedLabels.length === 0) {
        		return;
        	}
        	
        	
        	const divWrapper = angular.element(createElem('div')).addClass('card-labels')[0];
        	const ul = angular.element(createElem('ul')).addClass('labels')[0];
        	divWrapper.appendChild(ul);
        	for(var i = 0; i < userCreatedLabels.length; i++) {
        		const value = userCreatedLabels[i];
        		const bg = labelBackground(projectMetadata.labels[value.labelId].color);
        		
        		
        		//
        		const addSeparator = (value.labelValueType || value.type) !== 'NULL';
            	const name = projectMetadata.labels[value.labelId].name;
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
        		const li = angular.element(createElem('li'))
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
        
        function appendIconAndText(li, iconName, text) {
        	const icon = createElem('md-icon');
    		
    		$mdIcon(iconName).then(function(svg) {
        		icon.appendChild(svg);
        	});
    		li.appendChild(icon);
    		li.appendChild($window.document.createTextNode(' ' + text));
        }
        
        function createElem(name) {
        	return $window.document.createElement(name);
        }
        
        //-----------
        
		function handleUser(userId) {
			const a = createElem('a');
			UserCache.user(userId).then(function (user) {
				const element = angular.element(a);				
				element.attr('href', $state.href('user.dashboard', {provider: user.provider, username: user.username}));
				element.text($filter('formatUser')(user));
				if (!user.enabled) {
					element.addClass('user-disabled');
				}
			});
			return a;
		}
		
		
		function handleCard(cardId) {
			
			const a = createElem('a');
			
			
			CardCache.card(cardId).then(function (card) {
				const element = angular.element(a);
				
				a.textContent = card.boardShortName + '-' + card.sequence;
				element.attr('href', $state.href('board.card', {projectName: card.projectShortName, shortName: card.boardShortName, seqNr: card.sequence}));
				
				updateCardClass(card, element);
				
				const toDismiss = $rootScope.$on('refreshCardCache-' + cardId, function () {
					CardCache.card(cardId).then(function (card) {
						updateCardClass(card, element);
					});
				});
				listeners.push(toDismiss);
			});
			
			return a;
		}
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
	
	function addDueDateClasses($li, isTomorrow, isNow, isPast) {
    	if(isTomorrow) {
    		$li.addClass('lvg-due-date-tomorrow');
    	}
    	if(isNow) {
    		$li.addClass('lvg-due-date-now');
    	}
    	if(isPast) {
    		$li.addClass('lvg-due-date-past')
    	}
    }
})();