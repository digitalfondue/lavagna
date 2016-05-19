(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminExportImport', {
    	templateUrl: 'app/components/admin/export-import/export-import.html',
        bindings: {},
        controller: AdminExportImportController
    });

    function AdminExportImportController($window, Notification, Admin) {
        var ctrl = this;

        ctrl.overrideConfiguration = false;

        ctrl.doExport = function () {
            $("#export-iframe").remove();
            $($window.document.body).append('<iframe id="export-iframe" style="display: none !important"></iframe>');
            $window.document.getElementById('export-iframe').contentWindow.document.write('<html><head><base href="' + $('base').attr('href') + '"></head><body><form action="api/export" method="POST">'
                + '<input type="hidden" name="_csrf" value="' + $window.csrfToken + '"></form>'
                + '<script>document.forms[0].submit();</script></body></html>');
        }

        function importLavagnaCleanUp() {
            ctrl.importFile = null;
            ctrl.importing = false;

            /*$scope.$apply(function () {
                $scope.importing = false;
            });*/
        }

        function importLavagna(data, status) {
            if(status == 200) {
                notifySuccess();
            } else {
                notifyError();
            }
            importLavagnaCleanUp();
        }

        function importLavagnaError() {
            notifyError();
            importLavagnaCleanUp();
        }

        function notifySuccess() {
            Notification.addAutoAckNotification('success', {
                key: 'notification.admin-export-import.import.success'
            }, false);
        }

        function notifyError() {
            Notification.addAutoAckNotification('error', {
                key: 'notification.admin-export-import.import.error'
            }, false);
        }

        ctrl.importFile = null;
        ctrl.onFileSelect = function($files) {
            ctrl.importFile = $files[0]; //single file
        }

        ctrl.doImport = function () {

            if (ctrl.importFile == null) {
                return;
            }

            Admin.importData(ctrl.importFile, ctrl.overrideConfiguration, function() {}, importLavagna, importLavagnaError);
            ctrl.importing = true;
        }
    };
})();
