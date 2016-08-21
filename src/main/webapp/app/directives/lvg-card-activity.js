(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardActivity', function () {
		return {
			templateUrl: 'partials/fragments/card-activity-entry-fragment.html',
			restrict: 'E',
			scope: false
		}
	});

	directives.directive('lvgActivityColumn', function (BoardCache) {
		return {
			template: '<span class=\"lvg-activity-column-placeholder\"></span>',
			restrict: 'A',
			scope: {
				columnId: "=lvgActivityColumn"
			},
			link: function ($scope, element, attr) {
				var placeholder = element.find('.lvg-activity-column-placeholder');
				BoardCache.column($scope.columnId).then(function (column) {
					placeholder.text(column.columnName);
				});
			}
		}
	});

	directives.directive('lvgActivityLabel', function ($stateParams) {
		return {
			templateUrl: 'partials/fragments/card-activity-label-name-value-fragment.html',
			restrict: 'A',
			replace: true,
			scope: {
				activity: "=lvgActivityLabel"
			},
            link: function ($scope) {
			    $scope.project = $stateParams.shortName;
            }
		}
	});

	directives.directive('lvgActivityActionList', function (CardCache) {
		return {
			template: '<span data-bindonce=\"value\" data-bo-text=\"value.content\"></span>',
			restrict: 'A',
			scope: {
				actionlist: '=lvgActivityActionList',
				actionListId: '=',
				cardId: '='
			},
			link: function ($scope, element, attr) {
				$scope.value = $scope.actionlist;
				if ($scope.value === undefined) {
					CardCache.cardData($scope.actionListId).then(function (actionlist) {
						$scope.value = actionlist;
					});
				}
			}
		}
	});

	directives.directive('lvgActivityActionItem', function (CardCache) {
		return {
			template: '<span data-bindonce=\"value\" data-bo-text="value.content"></span>',
			restrict: 'A',
			scope: {
				actionitem: '=lvgActivityActionItem',
				actionItemId: '=',
				cardId: '='
			},
			link: function ($scope, element, attr) {
				$scope.value = $scope.actionitem;
				if ($scope.value === undefined) {
					CardCache.cardData($scope.actionItemId).then(function (actionitem) {
						$scope.value = actionitem;
					});
				}
			}
		}
	});

	directives.directive('lvgActivityFile', function () {
		return {
			template: '<span data-bindonce=\"value\" data-bo-text="value.name"></span>',
			restrict: 'A',
			scope: {
				file: '=lvgActivityFile',
				activity: '='
			},
			link: function ($scope) {
				$scope.value = $scope.file;
				if ($scope.value === undefined) {
					$scope.value = {
						name: $scope.activity.valueString
					};
				}
			}
		}
	});

	directives.directive('lvgActivityComment', function (CardCache) {
		return {
			template: '<a class=\"lvg-comment-link-placeholder\"><span class=\"lvg-comment-placeholder\"></span></a>',
			restrict: 'A',
			scope: {
				activity: '=lvgActivityComment'
			},
			link: function ($scope, element) {
				if ($scope.activity === undefined) {
					return;
				}
				CardCache.card($scope.activity.cardId).then(function (card) {

					var linkPlaceholder = element.find('.lvg-comment-link-placeholder');
					linkPlaceholder.attr('href', '/' + card.projectShortName + '/' + card.boardShortName + '-' + card.sequence + '#' + $scope.activity.dataId);

					var commentPlaceholder = element.find('.lvg-comment-placeholder');
					commentPlaceholder.text('#' + $scope.activity.dataId);
				});
			}
		};
	});
})();
