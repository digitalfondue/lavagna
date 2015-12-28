(function() {
    'use strict';

    var components =  angular.module('lavagna.components');

    components.component('lvgComponentAdmin', {
        bindings: {},
        controller: AdminController,
        controllerAs: 'adminCtrl',
        templateUrl: 'app/components/admin/admin.html'
    });

    function AdminController($scope, Admin, Title) {
        var ctrl = this;
        //console.log('set admin title');
        //Title.set('title.admin.home');

        $scope.$on('$locationChangeSuccess', function(e, n, o) {
            console.log('set admin title on location success');
            Title.set('title.admin.home');
        });

        Admin.checkHttpsConfiguration().then(function (res) {
            ctrl.httpsConfigurationCheck = res;
        });
    };
})();
