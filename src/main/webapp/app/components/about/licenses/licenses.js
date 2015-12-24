(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgLicenses', {
        bindings: {},
        controller: function($http) {
            var ctrl = this;
            $http.get('about/THIRD-PARTY.txt').success(function(res) {
                ctrl.thirdParty=res;
            });
        },
        controllerAs: 'licenses',
        templateUrl: 'app/components/about/licenses/licenses.html'
    });
})();
