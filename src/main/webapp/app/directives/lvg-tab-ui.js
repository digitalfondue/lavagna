(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgTabUi', function ($timeout) {

		return {
			restrict: 'A',
			transclude: false,
			controller: function ($scope, $element) {
				$scope.togglePanel = function (target) {
					//check if the clicked tab isn't already open
					if ($($element).find(".active-panel").length > 0 && target === $($element).find(".active-panel").first().attr('id'))
						return;

					//refresh the controls list
					$($element).find(".tab-controls > .lavagna-tab-controls > li > a").each(function (index) {
						//set the correct active element
						if ($(this).attr('data-lvg-tab-ui-target') === target) {
							$(this).addClass('active');
						} else {
							$(this).removeClass('active');
						}
					});
					//refresh the panel list
					$($element).find(".lavagna-tab-panel").each(function (index) {
						//set the correct active element
						if ($(this).attr('id') === target) {
							$(this).addClass('active-panel');
						} else {
							$(this).removeClass('active-panel');
						}
					});
				}
			},
			link: function ($scope, element, attrs) {
				/*
				 * Set the binding and activate the first element
				 */
				$(element).find(".tab-controls > .lavagna-tab-controls > li > a").each(function (index) {
					//add binding to each element
					$(this).click(function () {
						$scope.togglePanel($(this).attr('data-lvg-tab-ui-target'));
					});
					//while we are at it...
					if (index == 0) {
						$scope.togglePanel($(this).attr('data-lvg-tab-ui-target'));
					}
				});
			}

		}
	});
})();