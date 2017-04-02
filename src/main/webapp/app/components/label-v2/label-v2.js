(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgLabelV2', {
        template: '<lvg-label-val-v2 value-ref="$ctrl.value" project-metadata-ref="$ctrl.projectMetadata"></lvg-label-val-v2><span data-ng-transclude></span>',
        bindings: {
            valueRef: '&',
            projectMetadataRef: '&'
        },
        transclude: true,
        controller: ['$window', '$element', LabelV2Controller]
    });

    function LabelV2Controller($window, $element) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            ctrl.value = ctrl.valueRef();
            ctrl.projectMetadata = ctrl.projectMetadataRef();
        };

        ctrl.$postLink = function postLink() {
            var domElem = $element[0];
            var addSeparator = (ctrl.value.labelValueType || ctrl.value.type || ctrl.value.labelType) !== 'NULL';
            var name = (ctrl.projectMetadata && ctrl.projectMetadata.labels && angular.isDefined(ctrl.value.labelId) && ctrl.value.labelId !== null) ? ctrl.projectMetadata.labels[ctrl.value.labelId].name : ctrl.value.labelName;
            var nameAndSeparator = $window.document.createTextNode(name + (addSeparator ? ': ' : '' ));

            domElem.insertBefore(nameAndSeparator, domElem.firstChild);
        };
    }
}());
