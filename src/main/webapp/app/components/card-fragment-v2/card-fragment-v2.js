(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentV2', {
		templateUrl: 'app/components/card-fragment-v2/card-fragment-v2.html',
		bindings: {
			boardShortName: '@',
			projectShortName: '@',
			readOnly: '@',
			view: '@',
			cardReference:'&'
		},
		controller: CardFragmentV2Controller
	});
	
	
	function CardFragmentV2Controller($filter) {
		var ctrl = this;
		
		ctrl.card = ctrl.cardReference();
		
		ctrl.readOnly = ctrl.readOnly != undefined;
        ctrl.listView = ctrl.view != undefined && ctrl.view == 'list';
        ctrl.boardView = ctrl.view != undefined && ctrl.view == 'board';
        ctrl.searchView = ctrl.view != undefined && ctrl.view == 'search';
        
        ctrl.shortCardName = ctrl.card.boardShortName + ' - ' + ctrl.card.sequence;
        
        ctrl.isSelfWatching = function (cardLabels, userId) {
            return Card.isWatchedByUser(cardLabels, userId)
        };
        
        function hasCountGreaterThanZero(name) {
        	return getCountOrZero(name) > 0;
        }
        
        function getCountOrZero(name) {
        	return (ctrl.card.counts && ctrl.card.counts[name]) ? ctrl.card.counts[name].count : 0;
        }
        
        
        
        ctrl.hasActionListWithItems = hasCountGreaterThanZero('ACTION_CHECKED') || hasCountGreaterThanZero('ACTION_UNCHECKED');
        
        if(ctrl.hasActionListWithItems) {
        	var checkedCount = getCountOrZero('ACTION_CHECKED');
        	var uncheckedCount = getCountOrZero('ACTION_UNCHECKED');
        	ctrl.actionItemsSummary = (checkedCount) + '/' + (checkedCount + uncheckedCount);
        } else {
        	ctrl.actionItemsSummary = '';
        }
        
        ctrl.hasFiles = hasCountGreaterThanZero('FILE');
        ctrl.filesCount = ctrl.hasFiles ? getCountOrZero('FILE') : ''; 
        
        
        var dueDateLabels = filterSystemLabelByName('DUE_DATE');
        ctrl.hasDueDateLabel = dueDateLabels.length == 1;
        if(ctrl.hasDueDateLabel) {
        	ctrl.dueDateLabel = dueDateLabels[0];
        	
        	var notClosed = ctrl.card.columnDefinition !== 'CLOSED';
        	var daysDiff = $filter('daysDiff')(ctrl.dueDateLabel.labelValueTimestamp);
        	ctrl.dueDateClasses =  {
        			'lvg-due-date-tomorrow': (notClosed  && daysDiff == -1),
        			'lvg-due-date-now': (notClosed && daysDiff == 0),
        			'lvg-due-date-past': (notClosed && daysDiff > 0)
        	};
        } else {
        	ctrl.dueDateLabel = '';
        	ctrl.dueDateClasses = {};
        }
        
        function filterSystemLabelByName(labelName) {
        	return $filter('filter')(ctrl.card.labels, {labelDomain:'SYSTEM', labelName});
        }
	}
	
})();