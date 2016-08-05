(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminExportImport', {
    	templateUrl: 'app/components/admin/export-import/export-import.html',
        controller: AdminExportImportController
    });

    function AdminExportImportController($window, Notification, Admin) {
        var ctrl = this;

        ctrl.importFile = null;
        var uploader = ctrl.uploader = Admin.getImportDataUploader();

        ctrl.overrideConfiguration = false;

        uploader.onAfterAddingFile = function(fileItem) {
            ctrl.importFile = fileItem;
        };

        uploader.onBeforeUploadItem = function(fileItem) {
            fileItem.formData.push({overrideConfiguration: ctrl.overrideConfiguration});
            ctrl.importing = true;
        }

        function importLavagnaCleanUp() {
            ctrl.importFile = null;
            uploader.clearQueue();
            ctrl.importing = false;
            ctrl.overrideConfiguration = false;
        }

        uploader.onSuccessItem = function(fileItem, response, status, headers) {
            Notification.addAutoAckNotification('success', {
                key: 'notification.admin-export-import.import.success'
            }, false);
            importLavagnaCleanUp();
        };
        uploader.onErrorItem = function(fileItem, response, status, headers) {
            Notification.addAutoAckNotification('error', {
                key: 'notification.admin-export-import.import.error'
            }, false);
            importLavagnaCleanUp();
        };

        ctrl.doExport = function () {
            $("#export-iframe").remove();
            $($window.document.body).append('<iframe id="export-iframe" style="display: none !important"></iframe>');
            $window.document.getElementById('export-iframe').contentWindow.document.write('<html><head><base href="' + $('base').attr('href') + '"></head><body><form action="api/export" method="POST">'
                + '<input type="hidden" name="_csrf" value="' + $window.csrfToken + '"></form>'
                + '<script>document.forms[0].submit();</script></body></html>');
        }
    };
})();
