(function() {
    'use strict';

    var components =  angular.module('lavagna.components');

    components.component('lvgAdmin', {
        controller: AdminController,
        templateUrl: 'app/components/admin/admin.html'
    });

    function AdminController(Admin) {
        var ctrl = this;

        Admin.checkHttpsConfiguration().then(function (res) {
            ctrl.httpsConfigurationCheck = res;
        });
    };
})();
