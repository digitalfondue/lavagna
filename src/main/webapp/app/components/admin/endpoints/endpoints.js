(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminEndpoints', {
    	templateUrl: 'app/components/admin/endpoints/endpoints.html',
        bindings: {},
        controller: function(Admin) {
            var ctrl = this;
            Admin.endpointInfo().then(function (res) {
                ctrl.endpointInfo = res;
            });
        }
    });
})();
