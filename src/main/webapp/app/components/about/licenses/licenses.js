(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgAboutLicenses', {
        templateUrl: 'app/components/about/licenses/licenses.html',
        controller: ['$http', LicensesController]
    });

    function LicensesController($http) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            $http.get('about/THIRD-PARTY.txt').then(function (res) {
                ctrl.thirdParty = res.data;
            });
        };
    }
}());
