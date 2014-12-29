(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgBoardSidebar', function (Board, Card, User, StompClient, $rootScope, $stateParams) {
		var subscriptionScope = undefined;

		return {
			restrict: 'A',
			controller: function ($scope) {
			},
			link: function ($scope, $element, $attr) {
				
				$scope.switchLocation = function (location) {
					$("#sidebar-drop-zone").hide();

					if (subscriptionScope !== undefined) {
						subscriptionScope.$destroy();
					}
					
					

					subscriptionScope = $scope.$new();
					if($scope.sideBarLocation && $scope.sideBarLocation != 'NONE') {
						$scope.sideBarLoad(0);
						
						StompClient.subscribe(subscriptionScope, '/event/board/' + $scope.boardName + '/location/' + $scope.sideBarLocation + '/card', function () {
							$scope.sideBarLoad(0);
						});
					}
				};
				

				$scope.sideBarLoad = function (direction) {
					$scope.sidebarLoaded = false;
					$scope.sidebar = $scope.sidebar || {};
					if ($scope.sidebar[$scope.sideBarLocation] === undefined) {
						$scope.sidebar[$scope.sideBarLocation] = {currentPage: 0};
					}
					Board.cardsInLocationPaginated($scope.boardName, $scope.sideBarLocation, $scope.sidebar[$scope.sideBarLocation].currentPage + direction).then(function (res) {
						if (res.length === 0) {
							Board.cardsInLocationPaginated($scope.boardName, $scope.sideBarLocation, 0).then(function (res) {
								$scope.sidebar[$scope.sideBarLocation] = {currentPage: 0, found: res.slice(0, 10), hasMore: res.length === 11};
							});
						} else {
							$scope.sidebar[$scope.sideBarLocation] = {currentPage: $scope.sidebar[ $scope.sideBarLocation].currentPage + direction, found: res.slice(0, 10), hasMore: res.length === 11};
						}
						$scope.sidebarLoaded = true;
					});
				};
				
				$("#sidebar-drop-zone").sortable({
					receive: function (ev, ui) {
						var cardId = ui.item.attr('data-lvg-card-id');
						ui.item.data('hasUpdate', false); // disable the move card to column logic
						ui.item.hide();
						if (cardId !== undefined) {
							Card.moveAllFromColumnToLocation(ui.item.attr('data-lavagna-card-column-id'), [cardId], $scope.sideBarLocation);
						}
					}
				});

				//TODO: move this in another directive, and create a single sortable for each card :D
				User.hasPermission('MOVE_CARD', $stateParams.projectName).then(function () {
					$scope.sortableCardOptionsForSidebar = {
						connectWith: ".lavagna-board-cards",
						placeholder: "lavagna-card-placeholder",
						start: function (e, ui) {
							ui.placeholder.height(ui.helper.outerHeight());
							ui.item.data('initialColumnId', ui.item.attr('data-lavagna-card-column-id'));

						},
						stop: function (e, ui) {
							if (ui.item.data('hasUpdate')) {
								var cardId = parseInt(ui.item.attr('data-lvg-card-id'), 10);
								var oldColumnId = parseInt(ui.item.data('initialColumnId'), 10);
								var newColumnId = parseInt(ui.item.data('newColumnId'), 10);
								var ids = ui.item.parent().sortable("toArray", {attribute: 'data-lvg-card-id'}).map(function (i) {
									return parseInt(i, 10);
								});
								ui.item.addClass('lavagna-to-be-cleaned-up');
								ui.item.replaceWith(ui.item.clone());
								Board.moveCardToColumn(cardId, oldColumnId, newColumnId, {newContainer: ids});
							}
							ui.item.removeData('hasUpdate');
							ui.item.removeData('initialColumnId');
							ui.item.removeData('newColumnId');
						},
						update: function (e, ui) {
							ui.item.data('newColumnId', ui.item.parent().parent().parent().attr('data-lvg-column-id'));
							ui.item.data('hasUpdate', true);
						}
					};
				}, function () {
					$scope.sortableCardOptionsForSidebar = false;
				});
				$scope.$watch('sideBarLocation', function() {
					if($scope.sideBarLocation === undefined) {
						return;
					}
					$scope.switchLocation($scope.sideBarLocation);
				});

			}
		}
	});
})();