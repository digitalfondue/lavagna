(function () {
	'use strict';
	
	angular.module('lavagna.components').component('lvgCardFragmentV2', {
		templateUrl: 'app/components/card-fragment-v2/card-fragment-v2.html',
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
        
        
        function filterSystemLabelByName(labelName) {
        	return $filter('filter')(ctrl.card.labels, {labelDomain:'SYSTEM', labelName: labelName});
        }
        
        function isSelfWatching(cardLabels, userId) {
            return Card.isWatchedByUser(cardLabels, userId)
        }
	}
	
})();