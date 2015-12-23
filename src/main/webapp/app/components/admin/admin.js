(function() {
    'use strict';

    var components =  angular.module('lavagna.components');

    components.directive('lvgComponentAdmin', AdminComponent);

    function AdminComponent(Admin) {
        return {
            restrict: 'E',
            scope: true,
            bindToController: {},
            controller: AdminController,
            controllerAs: 'adminCtrl',
            templateUrl: 'app/components/admin/admin.html'
        }
    };

    function AdminController(Admin) {
        var ctrl = this;

        Admin.checkHttpsConfiguration().then(function (res) {
            ctrl.httpsConfigurationCheck = res;
        });
    };
})();
