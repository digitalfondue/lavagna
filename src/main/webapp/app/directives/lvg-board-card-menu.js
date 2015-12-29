(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	//FIXME this directive is hacky
	directives.directive('lvgBoardCardMenu', function ($compile, $filter, Card, Board, BulkOperations) {
		return {
			restrict: 'A',
			link: function($scope, element, attrs) {
				
				$scope.openCardMenu = function(card, columns) {
					
					//we create a specific scope for the card menu, as the current $scope can be destroyed at any time
					var $scopeForCardMenu = $scope.$new();
					
					var cleanup = function () {
						$('#cardModal,#cardModalBackdrop').removeClass('in');
						$('#cardModal,#cardModalBackdrop').remove();
						$("#cardBoardMenu").remove();
						$(document).unbind('keyup', escapeHandler);
						
						$scopeForCardMenu.$destroy();
					};
					
					var escapeHandler = function (e) {
						if (e.keyCode == 27) {
							cleanup();
						}
					};
					
					$scopeForCardMenu.close = function() {
						cleanup();
					}
					
					$scopeForCardMenu.move = function(card, toColumn) {
						if(angular.isUndefined(toColumn)) {
							return;
						}

						Card.findByColumn(toColumn.id).then(function(cards) {
							var ids = [];
							for (var i = 0;i<cards.length;i++) {
								ids.push(cards[i].id);
							}
							ids.push(card.id);
							return ids;
						}).then(function(ids) {
							Board.moveCardToColumn(card.id, card.columnId, toColumn.id, {newContainer: ids});
						}).then(cleanup);
					};
					
					$scopeForCardMenu.moveCard = function(card, location) {
						Card.moveAllFromColumnToLocation(card.columnId, [card.id], location).then(cleanup);
					};
					
					var top = $(element).offset().top;
					var left = $(element).offset().left;
					var size = $(element).width() + 2;
					var height = $(element).height() + 2;
					
					var windowWidth = $(window).width();
					var windowHeight = $(window).height();
					
					$scopeForCardMenu.moveColumns = $filter('filter')(columns, function(col) {return col.id != card.columnId});
					
					
					//hacky and ugly: to refactor
					$scopeForCardMenu.card = $scope.cardFragmentCtrl.card;
					$scopeForCardMenu.board = $scope.cardFragmentCtrl.board;
					$scopeForCardMenu.isSelfWatching = $scope.cardFragmentCtrl.isSelfWatching;
					$scopeForCardMenu.currentUserId = $scope.cardFragmentCtrl.currentUserId;
					$scopeForCardMenu.isAssignedToCard = $scope.cardFragmentCtrl.isAssignedToCard;
					
					var projectShortName = $scope.cardFragmentCtrl.project;
					
					
					// imported from column...
					$scopeForCardMenu.assignToCurrentUser = function(cardId, currentUserId) {
			            var cardByProject = {};
			            cardByProject[projectShortName] = [cardId];
			            BulkOperations.assign(cardByProject, {id: currentUserId});
			        };
			        
			        $scopeForCardMenu.removeAssignForCurrentUser = function(cardId, currentUserId) {
			            var cardByProject = {};
			            cardByProject[projectShortName] = [cardId];
			            BulkOperations.removeAssign(cardByProject, {id: currentUserId});
			        };

			        $scopeForCardMenu.watchCard = function(cardId, currentUserId) {
			            var cardByProject = {};
			            cardByProject[projectShortName] = [cardId];
			            BulkOperations.watch(cardByProject, {id: currentUserId});
			        };

			        $scopeForCardMenu.unWatchCard = function(cardId, currentUserId) {
			            var cardByProject = {};
			            cardByProject[projectShortName] = [cardId];
			            BulkOperations.unWatch(cardByProject, {id: currentUserId});
			        };
					//
					
					
					$(document).bind('keyup', escapeHandler);
					
					$("body").append($('<div id="cardModalBackdrop" class="lvg-modal-overlay lvg-modal-overlay-fade"></div>'));
					$("#cardModal,#cardModalBackdrop").addClass('in');
					
					var template = $compile('<div id="cardBoardMenu" data-bindonce><lvg-card-fragment data-view="board" data-read-only="true" data-card="card" data-board="board"></lvg-card-fragment>'
							+ '<div id="cardBoardMenuActions">'
							+ '<div id="cardBoardMenuClose"><button type="button" class="close" data-ng-click="close()">&times;</button></div>'
							+ '<div data-ng-include="\'partials/fragments/board-card-menu-card-actions-fragment.html\'"></div></div>'
							+ '</div>')($scopeForCardMenu);
					$("body").append(template);
					
					//card overflows
					if(windowHeight - top < height) {
						top = windowHeight - (height + 10);
					}
					
					$("#cardBoardMenu").css({
						'position' : 'fixed',
						'top' : top,
						'left' : left,
						'width' : size + 'px'
					});
					
					//calculate actions position
					var actionsCloseCSSObject = {};
					var actionsCSSObject = {};
					
					if(windowHeight - top < 260) {
						actionsCSSObject['bottom'] = 0;
					} else {
						actionsCSSObject['top'] = 0;
					}
					
					if(left + size + 30 > windowWidth - size) {
						actionsCSSObject['right'] = size + 10;
						actionsCloseCSSObject['right'] = 0;
						actionsCSSObject['text-align'] = "right";
					} else {
						actionsCSSObject['left'] = size + 10;
						actionsCloseCSSObject['left'] = 0;
						actionsCSSObject['text-align'] = "left";
					}
					$("#cardBoardMenuActions").css(actionsCSSObject);
					$("#cardBoardMenuClose").css(actionsCloseCSSObject);
					
				}
			}
		}
	});
})();