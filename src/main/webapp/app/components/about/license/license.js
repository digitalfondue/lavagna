(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgLicense', {
        bindings: {},
        controller: function($http) {
            var ctrl = this;

            $http.get('about/LICENSE-GPLv3.txt').success(function(res) {
                ctrl.license=res;
            });
        },
        controllerAs: 'license',
        templateUrl: 'app/components/about/license/license.html'
    });
})();
