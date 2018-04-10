(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdmin', {
        templateUrl: 'app/components/admin/admin.html',
        controller: [AdminController],
    });

    function AdminController() {
    }
}());
