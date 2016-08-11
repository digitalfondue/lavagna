(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentV2DataInfo', {
		template: '<div class="card-data"><ul class="data-info"></ul></div>',
        bindings: {
        	cardRef: '&',
        	projectMetadataRef: '&',
        },
        controller: ['$filter', '$element', '$window', '$mdIcon', lvgCardFragmentV2DataInfoCtrl]
	})
	
	
	
	function lvgCardFragmentV2DataInfoCtrl($filter, $element, $window, $mdIcon) {
		const ctrl = this;
		
		const ulElement = $element[0].querySelector('ul.data-info');
		
		ctrl.$postLink = function lvgCardFragmentV2DataInfoCtrlPostLink() {
			handleComment();
			handleActionList();
			handleFiles();
			handleDueDate();
			handleMilestone();
		}
		
		const card = ctrl.cardRef();
		const projectMetadata = ctrl.projectMetadataRef();
		
		// action list
        const hasActionListWithItems = hasCountGreaterThanZero('ACTION_CHECKED') || hasCountGreaterThanZero('ACTION_UNCHECKED');
        var actionItemsSummary = '';
        if(hasActionListWithItems) {
        	var checkedCount = getCountOrZero('ACTION_CHECKED');
        	var uncheckedCount = getCountOrZero('ACTION_UNCHECKED');
        	actionItemsSummary = (checkedCount) + '/' + (checkedCount + uncheckedCount);
        }
        //
        
        //
        
        //
        
        var notClosed = card.columnDefinition !== 'CLOSED';
        
        // due date related 
        var dueDateLabels = filterSystemLabelByName('DUE_DATE');
        ctrl.hasDueDateLabel = dueDateLabels.length == 1;
        ctrl.dueDateLabel = '';
    	ctrl.dueDateClasses = {};
        if(ctrl.hasDueDateLabel) {
        	ctrl.dueDateLabel = dueDateLabels[0];
        	
        	var daysDiff = $filter('daysDiff')(ctrl.dueDateLabel.labelValueTimestamp);
        	ctrl.dueDateClasses =  {
        			'lvg-due-date-tomorrow': (notClosed  && daysDiff == -1),
        			'lvg-due-date-now': (notClosed && daysDiff == 0),
        			'lvg-due-date-past': (notClosed && daysDiff > 0)
        	};
        }
        //
        
      //milestone related
        var milestoneLabels = filterSystemLabelByName('MILESTONE');
        ctrl.hasMilestoneLabel = milestoneLabels.length === 1;
        ctrl.milestoneLabel = '';
        ctrl.milestoneClasses = {};
        if (ctrl.hasMilestoneLabel) {
        	ctrl.milestoneLabel = milestoneLabels[0];
        	
        	var releaseDateStr = undefined;
        	const metadata = projectMetadata;
        	const milestoneLabel = ctrl.milestoneLabel;
        	if(metadata && metadata.labelListValues && milestoneLabel && milestoneLabel.labelValueList && metadata.labelListValues[milestoneLabel.labelValueList].metadata) {        	
        		releaseDateStr = metadata.labelListValues[milestoneLabel.labelValueList].metadata.releaseDate;
        	}	
        	
        	if(releaseDateStr) {
        		var releaseDate = moment(releaseDateStr, "DD.MM.YYYY");//FIXME format server side
        		var daysDiff = $filter('daysDiff')(releaseDate);
        		ctrl.milestoneClasses = {
            			'lvg-due-date-tomorrow': (notClosed  && daysDiff == -1),
            			'lvg-due-date-now': (notClosed && daysDiff == 0),
            			'lvg-due-date-past': (notClosed && daysDiff > 0)
        		};
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
        		return;
        	}
        	
        	const li = createElem('li');
    		
    		appendIconAndText(li, 'comment', card.counts['COMMENT'].count);
        	ulElement.appendChild(li);
        }
        
        function handleActionList() {
        	if(!hasActionListWithItems) {
        		return;
        	}
        	
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
        	ulElement.appendChild(li);
        }
        
        
        
        
        function handleFiles() {
        	const hasFiles = hasCountGreaterThanZero('FILE');
        	if(!hasFiles) {
        		return;
        	}
        	
        	const filesCount = hasFiles ? getCountOrZero('FILE') : '';
        	const li = createElem('li');
    		
    		appendIconAndText(li, 'file', filesCount);
        	
        	ulElement.appendChild(li);
        }
        
        function handleDueDate() {
        	if(!ctrl.hasDueDateLabel) {
        		return;
        	}
        	
        	const li = createElem('li');
    		const $li = angular.element(li);
    		
    		addDueDateClasses($li, ctrl.dueDateClasses);
    		
    		const value = ctrl.dueDateLabel.value || ctrl.dueDateLabel;
    		
    		appendIconAndText(li, 'clock', $filter('date')(value.valueTimestamp, 'dd.MM.yyyy'));
    		ulElement.appendChild(li);
        }
        
        function handleMilestone() {
        	if(!ctrl.hasMilestoneLabel) {
        		return;
        	}
        	
    		const li = createElem('li');
    		const $li = angular.element(li);
    		
    		addDueDateClasses($li, ctrl.milestoneClasses);

    		appendIconAndText(li, 'milestone', projectMetadata.labelListValues[ctrl.milestoneLabel.labelValueList].value);
    		ulElement.appendChild(li);
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
        
        function createElem(name) {
        	return $window.document.createElement(name);
        }
	}
	
	
})();