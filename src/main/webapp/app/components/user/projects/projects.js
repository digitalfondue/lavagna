(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentUserProjects', {
        bindings: {
            profile: '='
        },
        templateUrl: 'app/components/user/projects/projects.html'
    });

})();
