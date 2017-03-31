(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgUserProjects', {
        bindings: {
            profile: '<'
        },
        controller: ['$mdMedia', 'Project', UserProjectsController],
        templateUrl: 'app/components/user/projects/projects.html'
    });

    function UserProjectsController($mdMedia, Project) {
        var ctrl = this;

        ctrl.$mdMedia = $mdMedia;
        ctrl.activeProjectsLeft = [];
        ctrl.activeProjectsRight = [];

        ctrl.$onInit = function onInit() {
            var grid = Project.gridByDescription(ctrl.profile.activeProjects, true);

            ctrl.activeProjectsLeft = grid.left;
            ctrl.activeProjectsRight = grid.right;
        };
    }
}());
