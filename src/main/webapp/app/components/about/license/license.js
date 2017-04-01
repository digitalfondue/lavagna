(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgAboutLicense', {
        templateUrl: 'app/components/about/license/license.html',
        controller: ['$http', LicenseController]
    });

    function LicenseController($http) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            $http.get('about/LICENSE-GPLv3.txt').then(function (res) {
                ctrl.license = res.data;
            });
        };
    }
}());
