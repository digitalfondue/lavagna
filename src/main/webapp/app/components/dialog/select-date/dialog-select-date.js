(function () {
    'use strict';

    angular
        .module('lavagna.components')
        .component('lvgDialogSelectDate', {
            templateUrl: 'app/components/dialog/select-date/dialog-select-date.html',
            bindings: {
                dialogTitle: '<',
                action: '&'
            },
            controller: ['$mdDialog', DialogSelectDateController]
        });

    function DialogSelectDateController($mdDialog) {
        var ctrl = this;

        ctrl.cancel = cancel;
        ctrl.ok = ok;

        function cancel() {
            $mdDialog.hide();
        }

        function ok(date) {
            ctrl.action({'$date': date});
            $mdDialog.hide();
        }
    }
}());
