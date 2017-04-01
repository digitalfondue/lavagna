(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgColorPickerV2', {
        require: {ngModel: 'ngModel'},
        template: '<div role="button" class="lvg-color-picker-v2__button" ng-style="{\'background-color\': $ctrl.ngModel.$modelValue}" ng-click="$ctrl.showPicker($event)"></div>',
        controller: ['$mdPanel', '$element', ColorPickerV2Controller]
    });

    function ColorPickerV2Controller($mdPanel, $element) {
        var ctrl = this;

        ctrl.showPicker = showPicker;

        function showPicker(event) {
            var position = $mdPanel.newPanelPosition()
                .relativeTo($element.find('div'))
                .addPanelPosition($mdPanel.xPosition.ALIGN_START, $mdPanel.yPosition.BELOW)
                .addPanelPosition($mdPanel.xPosition.OFFSET_START, $mdPanel.yPosition.BELOW);

            var conf = {
                attachTo: angular.element(document.body),
                controller: PanelController,
                controllerAs: '$ctrl',
                template: panelTemplate,
                position: position,
                openFrom: event,
                clickOutsideToClose: true,
                escapeToClose: true,
                focusOnOpen: true,
                locals: {
                    ngModel: ctrl.ngModel
                }
            };

            $mdPanel.open(conf);
        }
    }

    var panelTemplate = '<div class="lvg-color-picker-v2__panel" md-whiteframe="2">'
                        + '<div role="button" class="lvg-color-picker-v2__button lvg-color-picker-v2__button_in-panel" ng-repeat="color in $ctrl.colors" ng-style="{\'background-color\' : color.value}" ng-click="$ctrl.selectColor(color)"><md-tooltip>{{::color.name}}</md-tooltip></div>'
                        + '</div>';

    function PanelController(mdPanelRef) {
        var ctrl = this;

        ctrl.colors = [{name: 'Red', value: '#F44336'},
                       {name: 'Pink', value: '#E91E63'},
                       {name: 'Purple', value: '#9C27B0'},
                       {name: 'Deep Purple', value: '#673AB7'},
                       {name: 'Indigo', value: '#3F51B5'},
                       {name: 'Blue', value: '#2196F3'},
                       {name: 'Light Blue', value: '#03A9F4'},
                       {name: 'Cyan', value: '#00BCD4'},
                       {name: 'Teal', value: '#009688'},
                       {name: 'Green', value: '#4CAF50'},
                       {name: 'Light Green', value: '#8BC34A'},
                       {name: 'Lime', value: '#CDDC39'},
                       {name: 'Yellow', value: '#FFEB3B'},
                       {name: 'Amber', value: '#FFC107'},
                       {name: 'Orange', value: '#FF9800'},
                       {name: 'Deep Orange', value: '#FF5722'},
                       {name: 'Brown', value: '#795548'},
                       {name: 'Grey', value: '#9E9E9E'},
                       {name: 'Blue Grey', value: '#607D8B'},
                       {name: 'Black', value: '#000000'}, ];

        ctrl.selectColor = selectColor;

        function selectColor(color) {
            ctrl.ngModel.$setViewValue(color.value);
            mdPanelRef.close();
        }
    }
}());
