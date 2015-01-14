(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardFragment', function (Card) {
		return {
			templateUrl: 'partials/fragments/card-fragment.html',
			restrict: 'E',
			scope: {
				card : '=card',
				project : '=project',
				board : '=board',
				selected : '=selected',
				query : '=query',
				page : '=page',
				dependencies : '=dependencies'
			},
			controller : function($scope, $element, $attrs) {
				//controls attributes
				$scope.readOnly = $attrs.readOnly != undefined;
				$scope.hideAssignedUsers = $attrs.hideAssignedUsers != undefined;
				
				$scope.listView = $attrs.view != undefined && $attrs.view == 'list';
				$scope.boardView = $attrs.view != undefined && $attrs.view == 'board';
				$scope.searchView = $attrs.view != undefined && $attrs.view == 'search';
				
				//dependencies
				angular.extend($scope, $scope.dependencies);
				
				$scope.hasUserLabels = function(cardLabels) {
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

				$scope.hasSystemLabelByName = function(labelName, cardLabels) {
					if (cardLabels === undefined || cardLabels.length === 0)
						return false; //empty, no labels at all
					for(var i = 0; i < cardLabels.length; i++) {
						if(cardLabels[i].labelName == labelName && cardLabels[i].labelDomain === 'SYSTEM') {
							return true;
						}
					}
					return false;
				};
			
				$scope.isSelfWatching = function(cardLabels, userId) {
					return Card.isWatchedByUser(cardLabels, userId)
				};

				$scope.isAssignedToCard = function(cardLabels, userId) {
					return Card.isAssignedToUser(cardLabels, userId);
				};
			}
		}
	});
})();