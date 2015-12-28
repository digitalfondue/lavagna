(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentProjectManage', {
        bidings: {
            project: '='
        },
        controller: function(Title) {
            var ctrl = this;
            Title.set('title.project.manage', { shortname: ctrl.project.shortName });
        },
        controllerAs: 'projManCtrl',
        templateUrl: 'app/components/project/manage/manage.html'
    });
})();
