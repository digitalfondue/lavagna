(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentV2', {
		templateUrl: 'app/components/card-fragment-v2/card-fragment-v2.html',
		bindings: {
			readOnly: '@',
			view: '@',
			cardRef:'&',
			userRef:'&',
			boardColumnsRef: '&',
			projectMetadataRef: '&'
		},
		controller: CardFragmentV2Controller
	});
	
	
	function CardFragmentV2Controller($filter, Card) {
		var ctrl = this;
		
		
		
		ctrl.card = ctrl.cardRef();
		//
		ctrl.boardShortName = ctrl.card.boardShortName,
		ctrl.projectShortName = ctrl.card.projectShortName,
		//
		ctrl.user = ctrl.userRef();
		ctrl.projectMetadata = ctrl.projectMetadataRef();
		
		ctrl.readOnly = ctrl.readOnly != undefined;
        ctrl.listView = ctrl.view != undefined && ctrl.view == 'list';
        ctrl.boardView = ctrl.view != undefined && ctrl.view == 'board';
        ctrl.searchView = ctrl.view != undefined && ctrl.view == 'search';
        
        ctrl.shortCardName = ctrl.card.boardShortName + ' - ' + ctrl.card.sequence;
        
        // action list
        ctrl.hasActionListWithItems = hasCountGreaterThanZero('ACTION_CHECKED') || hasCountGreaterThanZero('ACTION_UNCHECKED');
        ctrl.actionItemsSummary = '';
        if(ctrl.hasActionListWithItems) {
        	var checkedCount = getCountOrZero('ACTION_CHECKED');
        	var uncheckedCount = getCountOrZero('ACTION_UNCHECKED');
        	ctrl.actionItemsSummary = (checkedCount) + '/' + (checkedCount + uncheckedCount);
        }
        //
        
        //
        ctrl.isSelfWatching = Card.isWatchedByUser(ctrl.card.labels, ctrl.user.id);
        ctrl.isAssignedToCard = Card.isAssignedToUser(ctrl.card.labels, ctrl.user.id);
        //
        
        
        //
        ctrl.hasFiles = hasCountGreaterThanZero('FILE');
        ctrl.filesCount = ctrl.hasFiles ? getCountOrZero('FILE') : '';
        //
        
        var notClosed = ctrl.card.columnDefinition !== 'CLOSED';
        
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
        	
//        	try {
//        		var releaseDate = ctrl.projectMetadata.labelListValues[ctrl.milestoneLabel.labelValueList].metadata.releaseDate;
//        		var daysDiff = $filter('daysDiff')(releaseDate);
//        		ctrl.milestoneClasses = {
//            			'lvg-due-date-tomorrow': (notClosed  && daysDiff == -1),
//            			'lvg-due-date-now': (notClosed && daysDiff == 0),
//            			'lvg-due-date-past': (notClosed && daysDiff > 0)
//        		};
//        	} catch(e) {
//        	}
        }
        //
        
        
        // assigned related
        var assignedLabels = filterSystemLabelByName('ASSIGNED');
        ctrl.hasAssignedLabels = assignedLabels.length > 0;
        ctrl.assignedLabels = assignedLabels;
        //
        
        
        // user labels
        var userCreatedLabels = $filter('filter')(ctrl.card.labels, {labelDomain:'USER'});
        ctrl.hasUserCreatedLabels = userCreatedLabels.length;
        ctrl.userCreatedLabels = userCreatedLabels;
        //
        
        
        function hasCountGreaterThanZero(name) {
        	return getCountOrZero(name) > 0;
        }
        
        function getCountOrZero(name) {
        	return (ctrl.card.counts && ctrl.card.counts[name]) ? ctrl.card.counts[name].count : 0;
        }
        
        
        function filterSystemLabelByName(labelName) {
        	return $filter('filter')(ctrl.card.labels, {labelDomain:'SYSTEM', labelName});
        }
        
        function isSelfWatching(cardLabels, userId) {
            return Card.isWatchedByUser(cardLabels, userId)
        }
	}
	
})();