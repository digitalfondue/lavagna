(function() {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Head', {
		require : {
			lvgCardFragmentV2 : '^lvgCardFragmentV2'
		},
		controller : [ '$element', '$scope', lvgCardFragmentV2HeadCtrl ]
	});

	function lvgCardFragmentV2HeadCtrl($element, $scope) {
		const ctrl = this;
		ctrl.$postLink = function lvgCardFragmentV2HeadCtrlPostLink() {
			const parent = ctrl.lvgCardFragmentV2;

			if (parent.boardView && !parent.readOnly) {

			} else if (parent.boardView && parent.readOnly) {

			} else if (parent.listView) {

			} else if (parent.searchView) {
				
			}
		}
	}

})();