(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminEndpoints', {
        templateUrl: 'app/components/admin/endpoints/endpoints.html',
        controller: ['Admin', AdminEndpointsController]
    });

    function AdminEndpointsController(Admin) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            Admin.endpointInfo().then(function (res) {
                ctrl.endpointInfo = res;
            });
        };
    }
}());
