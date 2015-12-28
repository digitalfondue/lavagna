(function() {
    'use strict';

    var components =  angular.module('lavagna.components');

    components.component('lvgComponentAdmin', {
        bindings: {},
        controller: AdminController,
        controllerAs: 'adminCtrl',
        templateUrl: 'app/components/admin/admin.html'
    });

    function AdminController(Admin) {
        var ctrl = this;

        Admin.checkHttpsConfiguration().then(function (res) {
            ctrl.httpsConfigurationCheck = res;
        });
    };
})();
