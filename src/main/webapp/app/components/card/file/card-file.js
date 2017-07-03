(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgCardFile', {
        bindings: {
            file: '<',
            onDelete: '&'
        },
        controller: angular.noop,
        templateUrl: 'app/components/card/file/card-file.html'
    });
}());
