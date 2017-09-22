(function () {
    'use strict'

    angular.module('lavagna.components').component('integrationsAddParameter', {
        bindings: {
            parameter: '<',
            onRemove: '&'
        },
        templateUrl: 'app/components/admin/integrations/add-parameter/integrations-add-parameter.html'
    });
})();


