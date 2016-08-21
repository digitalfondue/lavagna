(function () {

    'use strict';

    var components = angular.module('lavagna.directives');

    components.component('lvgLabel', {
        template: '<lvg-label-val value="$ctrl.value" project="::$ctrl.project"></lvg-label-val><span data-ng-transclude></span>',
        bindings: {
            value: '=',
            project:'<'
        },
        transclude: true,
        controller: ['$window', '$element', 'ProjectCache', lvgLabelCtrl]
    });

    function lvgLabelCtrl($window, $element, ProjectCache) {
        var ctrl = this;
        var domElem = $element[0];

        ctrl.$postLink = function lvgLabelV2PostLink() {
            ProjectCache.getMetadata(ctrl.project).then(function (metadata) {
                var addSeparator = (ctrl.value.labelValueType || ctrl.value.type) !== 'NULL';
                var name = (metadata && metadata.labels) ? metadata.labels[ctrl.value.labelId].name : ctrl.value.labelName;
                var nameAndSeparator = $window.document.createTextNode(name + (addSeparator ? ': ' : '' ));
                domElem.insertBefore(nameAndSeparator, domElem.firstChild);
            });
        }
    }

})();
