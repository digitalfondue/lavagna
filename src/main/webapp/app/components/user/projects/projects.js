(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentUserProjects', {
        bindings: {
            profile: '='
        },
        controller: function() { var ctrl = this; },
        controllerAs: 'userProjectsCtrl',
        templateUrl: 'app/components/user/projects/projects.html'
    });

})();
