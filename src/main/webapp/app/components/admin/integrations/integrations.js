(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminIntegrations', {
    	templateUrl: 'app/components/admin/integrations/integrations.html',
        controller: [AdminIntegrationsController]
    });


    function AdminIntegrationsController() {
    }

})()
