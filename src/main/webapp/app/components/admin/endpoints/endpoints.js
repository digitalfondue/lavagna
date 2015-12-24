(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentAdminEndpoints', {
        bindings: {},
        controller: function(Admin) {
            var ctrl = this;
            Admin.endpointInfo().then(function (res) {
                ctrl.endpointInfo = res;
            });
        },
        controllerAs: 'adminEndpointCtrl',
        templateUrl: 'app/components/admin/endpoints/endpoints.html'
    });
})();
