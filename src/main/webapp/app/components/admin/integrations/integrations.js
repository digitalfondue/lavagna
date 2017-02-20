(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminIntegrations', {
    	templateUrl: 'app/components/admin/integrations/integrations.html',
        controller: ['Integrations', AdminIntegrationsController]
    });


    function AdminIntegrationsController(Integrations) {

        var ctrl = this;

        ctrl.$onInit = function() {
            Integrations.getAll().then(function(res) {
                ctrl.integrations = res;
            });
        }
    }

})()
