(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgStatsPanelProject', {
        bindings: {
            project: '<',
            hideMenu: '<?'
        },
        controller: ['Project', StatsPanelProjectController],
        templateUrl: 'app/components/stats/panel-project/stats-panel-project.html'
    });

    function StatsPanelProjectController(Project) {
        var ctrl = this;

        ctrl.statsFetcher = statsFetcher;
        ctrl.archive = archive;
        ctrl.unarchive = unarchive;

        function statsFetcher() {
            return Project.taskStatistics(ctrl.project.shortName);
        }

        function archive() {
            update(ctrl.project.shortName, ctrl.project.name, ctrl.project.description, true);
        }

        function unarchive() {
            update(ctrl.project.shortName, ctrl.project.name, ctrl.project.description, false);
        }

        function update(shortName, name, description, isArchived) {
            Project.update({
                shortName: shortName,
                name: name,
                description: description,
                archived: isArchived
            });
        }
    }
}());
