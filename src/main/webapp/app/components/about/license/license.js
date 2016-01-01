(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAboutLicense', {
        controller: function($http) {
            var ctrl = this;

            $http.get('about/LICENSE-GPLv3.txt').success(function(res) {
                ctrl.license=res;
            });
        },
        templateUrl: 'app/components/about/license/license.html'
    });
})();
