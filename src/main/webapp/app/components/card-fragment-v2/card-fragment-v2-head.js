(function() {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Head', {
		require : {
			lvgCardFragmentV2 : '^lvgCardFragmentV2'
		},
		controller : [ '$element', lvgCardFragmentV2HeadCtrl ]
	});

	function lvgCardFragmentV2HeadCtrl($element) {
		const
		ctrl = this;

		ctrl.$onInit = function() {
		}

		ctrl.$postLink = function() {
		}
	}

})();