(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgLabelV2', {
    	template: '<span>'
			+'<span ng-bind="::$ctrl.name"></span>'
			+'<span ng-if="::($ctrl.type !== \'NULL\')">: </span>'
			+'<lvg-label-val-v2 value-ref="::$ctrl.value" project-metadata-ref="::$ctrl.projectMetadata"></lvg-label-val-v2>'
			+'</span>',
    	bindings: {
    		nameRef: '&',
    		valueRef: '&',
			projectMetadataRef:'&'
    	},
    	controller: function() {
    		var ctrl = this;
    		ctrl.name = ctrl.nameRef();
    		ctrl.value = ctrl.valueRef();
    		ctrl.projectMetadata = ctrl.projectMetadataRef();
    		ctrl.type = ctrl.value.labelValueType || ctrl.value.type;
    	}
    })
})();