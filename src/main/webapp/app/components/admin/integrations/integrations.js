(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminIntegrations', {
    	templateUrl: 'app/components/admin/integrations/integrations.html',
        controller: ['$mdDialog','Integrations', AdminIntegrationsController]
    });


    function AdminIntegrationsController($mdDialog, Integrations) {

        var ctrl = this;

        ctrl.addNewIntegrationDialog = addNewIntegrationDialog;

        ctrl.$onInit = function() {
            Integrations.getAll().then(function(res) {
                ctrl.integrations = res;
            });
        };

        function addNewIntegrationDialog() {
            $mdDialog.show({
                templateUrl: 'app/components/admin/integrations/add-new-integration-dialog.html',
                controller: function() {
                },
                controllerAs: 'addNewIntegrationCtrl',
                bindToController: true
            });
        }
    }

})()
