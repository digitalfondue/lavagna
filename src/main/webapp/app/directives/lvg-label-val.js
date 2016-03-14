(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgLabelVal', function ($filter, $compile, StompClient, LabelCache) {

		var loadListValue = function (labelId, listValueId, scope) {
			return LabelCache.findLabelListValue(labelId, listValueId).then(function (listValue) {
				scope.displayValue = listValue.value;
				scope.metadata = listValue.metadata;
			});
		};
		
		var labelValTemplate = function($element, $attrs) {
			
			var readOnly = $attrs.readOnly != undefined;
			
			function addReadOnlyAttr() {
				return readOnly ? ' data-read-only ' : ''
			}
			
			return '<span ng-if="::(type === \'USER\')" ng-class="::{\'strike\' : metadata.status === \'CLOSED\'}"><span '+addReadOnlyAttr()+' data-lvg-user-tooltip="displayValue"></span></span>'
				 + '<span ng-if="::(type === \'CARD\')" ng-class="::{\'strike\' : metadata.status === \'CLOSED\'}"><span data-no-name data-lvg-card-tooltip="displayValue" ' + addReadOnlyAttr() + '></span></span>'
				 + '<span ng-if="::(type != \'USER\' && type != \'CARD\')" ng-bind="::displayValue" ng-class="::{\'strike\' : metadata.status === \'CLOSED\'}"></span></span>';			
		}

		return {
			restrict: 'E',
			scope: {
				value: '<'
			},
			template: labelValTemplate,
			link: function ($scope, $element, $attrs) {

				if ($scope.value === undefined || $scope.value === null) {
					return;
				}
				
				$scope.type = $scope.value.labelValueType || $scope.value.type || $scope.value.labelType;

				var type = $scope.type;
				var value = $scope.value.value || $scope.value;
				if (type === 'STRING') {
					$scope.displayValue = value.valueString;
				} else if (type === 'INT') {
					$scope.displayValue = value.valueInt;
				} else if (type === 'USER') {
					$scope.displayValue = value.valueUser;
				} else if (type === 'CARD') {
					$scope.displayValue = value.valueCard;
				} else if (type === 'LIST') {
					loadListValue($scope.value.labelId, value.valueList, $scope);
					

					StompClient.subscribe($scope, '/event/label-list-values/' + value.valueList, function (message) {
						loadListValue($scope.value.labelId, value.valueList, $scope).then(function() {
							$element.html($compile(labelValTemplate($element, $attrs))($scope));
						})
					});
					
				} else if (type === 'TIMESTAMP') {
					$scope.displayValue = $filter('date')(value.valueTimestamp, 'dd.MM.yyyy');
				}
			}
		};
	});

})();