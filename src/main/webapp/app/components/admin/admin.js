(function() {
    'use strict';

    var components =  angular.module('lavagna.components');

    components.component('lvgAdmin', {
        templateUrl: 'app/components/admin/admin.html',
        controller: ['Admin', AdminController],
    });

    function AdminController(Admin) {
        var ctrl = this;

        Admin.checkHttpsConfiguration().then(function (res) {
            ctrl.httpsConfigurationCheck = res;
        });
    };
})();
