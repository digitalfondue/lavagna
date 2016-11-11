(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgUserProjects', {
        bindings: {
            profile: '<'
        },
        controller: ['$mdMedia', UserProjectsController],
        templateUrl: 'app/components/user/projects/projects.html'
    });

    function UserProjectsController($mdMedia) {
        var ctrl = this;

        ctrl.$mdMedia = $mdMedia;
        ctrl.activeProjectsLeft = [];
        ctrl.activeProjectsRight = [];

        ctrl.$onInit = function onInit() {
            loadActiveProjects(ctrl.profile.activeProjects);
        };

        function loadActiveProjects(projects) {
            var activeProjectsLeft = [];
            var activeProjectsRight = [];

            var rightCount = 0;
            var leftCount = 0;

            for(var i = 0; i < projects.length; i++) {
                var project = projects[i].project;
                if(project.archived) {
                    continue;
                }
                var descriptionCount = project.description != null ? project.description.length : 0;
                if(descriptionCount > 0) {
                    var newLineMatch = project.description.match(/[\n\r]/g);
                    descriptionCount += newLineMatch != newLineMatch ? newLineMatch.length * 50 : 0;
                }

                if(leftCount <= rightCount) {
                    leftCount += descriptionCount;
                    activeProjectsLeft.push(projects[i]);
                } else {
                    rightCount += descriptionCount;
                    activeProjectsRight.push(projects[i]);
                }
            }

            ctrl.activeProjectsLeft = activeProjectsLeft;
            ctrl.activeProjectsRight = activeProjectsRight;
        }
    }

})();
