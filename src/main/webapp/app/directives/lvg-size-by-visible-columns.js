(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgSizeByVisibleColumns', function (User, $stateParams) {
		return {
			restrict: 'A',
			scope: {
				columns: '=lvgSizeByVisibleColumns'
			},
			link: function ($scope, element, attrs) {
				//select all li that have no lavagna-hide class

				$scope.$watch('columns.length', function (length) {
					if(length === undefined) {
						return;
					}
					User.hasPermission("CREATE_COLUMN", $stateParams.projectName).then(function () {
						// count add column
						var width = 290+ 4* 2+ (length* 290+ 4* 2* length);
						element.css('width', width + "px");
					}).catch(function() {
						var width = length * 290 + 4* 2* length;
                        element.css('width', width + "px");
					});
				});
			}
		};
	})
})();