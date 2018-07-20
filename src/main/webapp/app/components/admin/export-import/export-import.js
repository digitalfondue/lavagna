(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminExportImport', {
        templateUrl: 'app/components/admin/export-import/export-import.html',
        controller: ['$window', 'Notification', 'Admin', AdminExportImportController]
    });

    function AdminExportImportController($window, Notification, Admin) {
        var ctrl = this;

        ctrl.importFile = null;
        var uploader = ctrl.uploader = Admin.getImportDataUploader();

        ctrl.overrideConfiguration = false;

        uploader.onAfterAddingFile = function (fileItem) {
            ctrl.importFile = fileItem;
        };

        uploader.onBeforeUploadItem = function (fileItem) {
            fileItem.formData.push({overrideConfiguration: ctrl.overrideConfiguration});
            ctrl.importing = true;
        };

        function importLavagnaCleanUp() {
            ctrl.importFile = null;
            uploader.clearQueue();
            ctrl.importing = false;
            ctrl.overrideConfiguration = false;
        }

        uploader.onSuccessItem = function () {
            Notification.addAutoAckNotification('success', {
                key: 'notification.admin-export-import.import.success'
            }, false);
            importLavagnaCleanUp();
        };
        uploader.onErrorItem = function () {
            Notification.addAutoAckNotification('error', {
                key: 'notification.admin-export-import.import.error'
            }, false);
            importLavagnaCleanUp();
        };

        ctrl.doExport = function () {
            var exportIframe = $window.document.querySelector('#export-iframe');

            if (exportIframe) {
                exportIframe.remove();
            }

            var iframe = $window.document.createElement('iframe');

            angular.element(iframe).attr('id', 'export-iframe').attr('style', 'display: none !important');
            $window.document.body.appendChild(iframe);

            var baseHref = angular.element($window.document.querySelector('base')).attr('href');
            var iframeDoc = iframe.contentWindow.document;

            function elem(name) {
                return iframeDoc.createElement(name);
            }

            if(baseHref !== '/') {
                iframeDoc.head.appendChild(angular.element(elem('base')).attr('href', baseHref)[0]);
            }

            var form = angular.element(elem('form')).attr('action', 'api/export').attr('method', 'POST')[0];

            form.appendChild(angular.element(elem('input')).attr('type', 'hidden').attr('name', '_csrf').attr('value', $window.csrfToken)[0]);

            iframeDoc.body.appendChild(form);

            var script = elem('script');

            script.textContent = 'document.forms[0].submit();';
            iframeDoc.body.appendChild(script);
        };
    }
}());
