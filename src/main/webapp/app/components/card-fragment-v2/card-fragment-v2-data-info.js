(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentV2DataInfo', {
		require : {
			lvgCardFragmentV2 : '^lvgCardFragmentV2'
		},
        controller: ['$filter', '$element', '$window', '$mdIcon', lvgCardFragmentV2DataInfoCtrl]
	})
	
	
	
	function lvgCardFragmentV2DataInfoCtrl($filter, $element, $window, $mdIcon) {
		const ctrl = this;
		
		var card;
		var projectMetadata;
		var notClosed;
		
		ctrl.$onInit = function lvgCardFragmentV2DataInfoCtrlOnInit() {
			card = ctrl.lvgCardFragmentV2.card;
			projectMetadata = ctrl.lvgCardFragmentV2.projectMetadata;
	        notClosed = card.columnDefinition !== 'CLOSED';
		}
		
		ctrl.$postLink = function lvgCardFragmentV2DataInfoCtrlPostLink() {
			const liComment = handleComment();
			const liActionList = handleActionList();
			const liFiles = handleFiles();
			const liDueDate = handleDueDate();
			const liMilestone = handleMilestone();
			
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
		}
		
		function appendIfNotNull(parent, child) {
			if(child) {
				parent.appendChild(child);
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
    		if(counts['ACTION_CHECKED'].count == counts['ACTION_CHECKED'].count + counts['ACTION_UNCHECKED'].count) {
    			$li.addClass('lvg-action-full');
    		}
    		if(card.columnDefinition == 'CLOSED' && counts['ACTION_CHECKED'].count != counts['ACTION_CHECKED'].count + counts['ACTION_UNCHECKED'].count) {
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
        	
        	const dueDateClasses =  {
        			'lvg-due-date-tomorrow': (notClosed  && daysDiff == -1),
        			'lvg-due-date-now': (notClosed && daysDiff == 0),
        			'lvg-due-date-past': (notClosed && daysDiff > 0)
        	};

        	
        	const li = createElem('li');
    		const $li = angular.element(li);
    		
    		addDueDateClasses($li, dueDateClasses);
    		
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
        	
        	var milestoneLabel = '';
            var milestoneClasses = {};

        	milestoneLabel = milestoneLabels[0];
        	
        	var releaseDateStr = undefined;
        	const metadata = projectMetadata;
        	if(metadata && metadata.labelListValues && milestoneLabel && milestoneLabel.labelValueList && metadata.labelListValues[milestoneLabel.labelValueList].metadata) {        	
        		releaseDateStr = metadata.labelListValues[milestoneLabel.labelValueList].metadata.releaseDate;
        	}	
        	
        	if(releaseDateStr) {
        		var releaseDate = moment(releaseDateStr, "DD.MM.YYYY");//FIXME format server side
        		var daysDiff = $filter('daysDiff')(releaseDate);
        		milestoneClasses = {
            			'lvg-due-date-tomorrow': (notClosed  && daysDiff == -1),
            			'lvg-due-date-now': (notClosed && daysDiff == 0),
            			'lvg-due-date-past': (notClosed && daysDiff > 0)
        		};
        	}
            
        	
        	
    		const li = createElem('li');
    		const $li = angular.element(li);
    		
    		addDueDateClasses($li, milestoneClasses);

    		appendIconAndText(li, 'milestone', projectMetadata.labelListValues[milestoneLabel.labelValueList].value);
    		return li;
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
	}
	
	function addDueDateClasses($li, classes) {
    	if(classes['lvg-due-date-tomorrow']) {
    		$li.addClass('lvg-due-date-tomorrow');
    	}
    	if(classes['lvg-due-date-now']) {
    		$li.addClass('lvg-due-date-now');
    	}
    	if(classes['lvg-due-date-past']) {
    		$li.addClass('lvg-due-date-past')
    	}
    }
	
	
})();