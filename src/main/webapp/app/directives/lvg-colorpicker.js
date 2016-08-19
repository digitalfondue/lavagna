(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgColorpicker', function () {
		return {
			restrict: 'E',
			require: 'ngModel',
			scope: false,
			template: '<input class="input-small" />',
			link: function ($scope, $element, attrs, $ngModel) {

				var $input = jQuery($element.find('input'));
				var fallbackValue = $scope.$eval(attrs.fallbackValue);

				function setViewValue(color) {
					var value = fallbackValue;

					if (color) {
						value = color.toString();
					} else if (angular.isUndefined(fallbackValue)) {
						value = color;
					}

					$ngModel.$setViewValue(value);
				}

				var onChange = function (color) {
					$scope.$apply(function () {
						setViewValue(color.toHexString());
					});
				};
				var onToggle = function () {
					$input.spectrum('toggle');
					return false;
				};
				var options = angular.extend({
					color: $ngModel.$viewValue,
					change: onChange,
					move: onChange,
					hide: onChange,
					showPalette: true,
					palette: [
					          ['#e51c23', '#e91e63', '#9c27b0', '#673ab7'], 
					          ['#3f51b5', '#5677fc', '#03a9f4', '#00bcd4'],
					          ['#009688', '#259b24', '#8bc34a', '#cddc39'],
					          ['#ffeb3b', '#ffc107', '#ff9800', '#ff5722']
					]
				}, $scope.$eval(attrs.options));


				if (attrs.triggerId) {
					angular.element(document.body).on('click', '#' + attrs.triggerId, onToggle);
				}

				$ngModel.$render = function () {
					$input.spectrum('set', $ngModel.$viewValue || '');
				};

				if (options.color) {
					$input.spectrum('set', options.color || '');
					setViewValue(options.color.toHexString());
				}

				$input.spectrum(options);

				$scope.$on('$destroy', function () {
					$input.spectrum('destroy');
				});
			}
		};
	});
})();
