(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgLabelValV2', {
		template: '<span ng-bind="::$ctrl.displayValue"></span>',
		bindings: {
			valueRef: '&',
			projectMetadataRef:'&'
		},
		controller: function($filter) {
			var ctrl = this;
			ctrl.value = ctrl.valueRef();
			
			var metadata = ctrl.projectMetadataRef();
			
			var type = ctrl.value.labelValueType || ctrl.value.type || ctrl.value.labelType;
			var value = ctrl.value.value || ctrl.value;
			
			ctrl.type = type;
			
			ctrl.displayValue = '';
			
			if (type === 'STRING') {
				ctrl.displayValue = value.valueString;
			} else if (type === 'INT') {
				ctrl.displayValue = value.valueInt;
			} else if (type === 'USER') {
				ctrl.displayValue = '__USER__' + value.valueUser;
				//FIXME
			} else if (type === 'CARD') {
				ctrl.displayValue = '__CARD__' + value.valueCard;
				//FIXME
			} else if (type === 'LIST' && metadata && metadata.labelListValues[value.valueList]) {
				ctrl.displayValue = metadata.labelListValues[value.valueList].value;
			} else if (type === 'TIMESTAMP') {
				ctrl.displayValue = $filter('date')(value.valueTimestamp, 'dd.MM.yyyy');
			}
		}
	});
})();