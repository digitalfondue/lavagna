(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgCardFragment', {
        templateUrl: 'app/components/common/card-fragment/card-fragment.html',
        bindings: {
            card : '=',
            project : '=',
            board : '=',
            selected : '=',
            query : '=',
            page : '=',
            dependencies : '=',
            view : '@',
            readOnly : '@'
        },
        controller : CardFragmentController,
        controllerAs : 'cardFragmentCtrl'
    });

	function CardFragmentController(Card, User) {
	    var ctrl = this;

        ctrl.readOnly = ctrl.readOnly != undefined;
        ctrl.hideAssignedUsers = ctrl.hideAssignedUsers != undefined;

        ctrl.listView = ctrl.view != undefined && ctrl.view == 'list';
        ctrl.boardView = ctrl.view != undefined && ctrl.view == 'board';
        ctrl.searchView = ctrl.view != undefined && ctrl.view == 'search';

        //dependencies
        angular.extend(ctrl, ctrl.dependencies);

        User.currentCachedUser().then(function(user) {
        	ctrl.currentUserId = user.id;
        });

        ctrl.hasUserLabels = function(cardLabels) {
            if (cardLabels === undefined || cardLabels.length === 0) {
                return false; //empty, no labels at all
            }
            for(var i = 0; i < cardLabels.length; i++) {
                if(cardLabels[i].labelDomain === 'USER') {
                    return true;
                }
            }
            return false;
        };

        ctrl.hasSystemLabelByName = function(labelName, cardLabels) {
            if (cardLabels === undefined || cardLabels.length === 0)
                return false; //empty, no labels at all
            for(var i = 0; i < cardLabels.length; i++) {
                if(cardLabels[i].labelName == labelName && cardLabels[i].labelDomain === 'SYSTEM') {
                    return true;
                }
            }
            return false;
        };

        ctrl.isSelfWatching = function(cardLabels, userId) {
            return Card.isWatchedByUser(cardLabels, userId)
        };

        ctrl.isAssignedToCard = function(cardLabels, userId) {
            return Card.isAssignedToUser(cardLabels, userId);
        };
	}
})();
