(function() {
	'use strict';

	angular.module('lavagna.components').component('lvgCardFragmentV2Head', {
		require : {
			lvgCardFragmentV2 : '^lvgCardFragmentV2'
		},
		controller : [ '$element', '$compile', '$scope', lvgCardFragmentV2HeadCtrl ]
	});

	function lvgCardFragmentV2HeadCtrl($element, $compile, $scope) {
		const ctrl = this;

		ctrl.$postLink = function lvgCardFragmentV2HeadCtrlPostLink() {
			$element.append($compile(template(ctrl.lvgCardFragmentV2))($scope));
		}
	}
	
	function template(ctrl) {
		
		var t = ''
		
		if(ctrl.boardView && !ctrl.readOnly) {
			t += '<div>';
			t += '<lvg-card-fragment-link dynamic-link="true" target-state="board.card" project-name="{{::$ctrl.lvgCardFragmentV2.projectShortName}}" board-short-name="{{::$ctrl.lvgCardFragmentV2.boardShortName}}" sequence-number="{{::$ctrl.lvgCardFragmentV2.card.sequence}}"></lvg-card-fragment-link>';
			t += '</div>'
			
		} else if (ctrl.boardView && ctrl.readOnly) {
			
		} else if (ctrl.listView) {
			
		} else { //ctrl.searchView
			
		}
		
		t += '<span ng-bind="::$ctrl.lvgCardFragmentV2.card.name"></span>'
		
		return t;
	}

})();