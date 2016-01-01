(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAboutLicenses', {
        controller: function($http) {
            var ctrl = this;
            $http.get('about/THIRD-PARTY.txt').success(function(res) {
                ctrl.thirdParty=res;
            });
        },
        templateUrl: 'app/components/about/licenses/licenses.html'
    });
})();
