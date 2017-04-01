(function () {
    'use strict';

    angular
        .module('lavagna.components')
        .component('lvgDialogSelectLabel', {
            templateUrl: 'app/components/dialog/select-label/dialog-select-label.html',
            bindings: {
                dialogTitle: '<',
                buttonLabel: '<',
                action: '&', // $label, $value
                withLabelValuePicker: '<',
                projectName: '<'
            },
            controller: ['$mdDialog', 'Project', '$filter', DialogSelectLabelController]
        });

    function DialogSelectLabelController($mdDialog, Project, $filter) {
        var ctrl = this;

        ctrl.ok = ok;
        ctrl.cancel = cancel;

        ctrl.$onInit = function init() {
            ctrl.selectedLabel = {};

            Project.getMetadata(ctrl.projectName).then(function (res) {
                ctrl.userLabels = $filter('orderBy')(res.userLabels, 'name');
            });
        };

        function cancel() {
            $mdDialog.hide();
        }

        function ok(label, value) {
            ctrl.action({'$label': label, '$value': value});
            $mdDialog.hide();
        }
    }
}());
