(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgAboutLicense', {
    	templateUrl: 'app/components/about/license/license.html',
        controller: ['$http', licenseCtrl]
    });
    
    
    function licenseCtrl($http) {
        var ctrl = this;

        ctrl.$onInit = function init() {
            $http.get('about/LICENSE-GPLv3.txt').success(function(res) {
                ctrl.license=res;
            });
        }
    }
    
})();
