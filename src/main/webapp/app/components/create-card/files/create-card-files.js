(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCreateCardFiles', {
        bindings: {
            'files': '<',
            'user': '<',
            'onUpdate': '&'
        },
        controller: ['Card', CreateCardFilesController],
        templateUrl: 'app/components/card/files/card-files.html'
    });

    function CreateCardFilesController(Card) {
        var ctrl = this;

        ctrl.$onInit = function () {
            ctrl.uploader = Card.getNewCardFileUploader();

            // callback status
            ctrl.uploader.onSuccessItem = function (fileItem, response) {
                if (getFileIndex(response[0]) === -1) {
                    ctrl.files.push({
                        digest: response[0],
                        name: fileItem.file.name,
                        size: fileItem.file.size,
                        userId: ctrl.user.id
                    });

                    ctrl.onUpdate({$files: ctrl.files});
                }

                ctrl.uploader.removeFromQueue(fileItem);
            };

            ctrl.uploader.onCancelItem = function (fileItem) {
                ctrl.uploader.removeFromQueue(fileItem);
            };
        };

        ctrl.delete = function ($file) {
            var deleteIdx = getFileIndex($file.digest);

            if (deleteIdx !== -1) {
                ctrl.files.splice(deleteIdx, 1);
            }

            ctrl.onUpdate({$files: ctrl.files});
        };

        function getFileIndex(digest) {
            var fileIdx = -1;

            angular.forEach(ctrl.files, function (file, idx) {
                if (file.digest === digest) {
                    fileIdx = idx;
                }
            });

            return fileIdx;
        }
    }
})();
