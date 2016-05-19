(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgAboutLicense', {
    	templateUrl: 'app/components/about/license/license.html',
        controller: function($http) {
            var ctrl = this;

            $http.get('about/LICENSE-GPLv3.txt').success(function(res) {
                ctrl.license=res;
            });
        }
    });
})();
