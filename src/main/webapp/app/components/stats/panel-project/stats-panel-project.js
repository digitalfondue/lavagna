(function() {

	'use strict';

	angular.module('lavagna.components').component('lvgStatsPanelProject', {
        template : '<lvg-stats-panel-simple item="$ctrl.project" stats-fetcher="$ctrl.statsFetcher()">' +
                       '<a data-ui-sref="project.boards({projectName: $ctrl.project.shortName})">{{$ctrl.project.shortName}} - {{$ctrl.project.name}}</a>' +
                   '</lvg-stats-panel-simple>',
        bindings : {
            project: '<'
        },
        controller : ['Project', StatsPanelProjectController]
    });

	function StatsPanelProjectController(Project) {

		var ctrl = this;

		ctrl.statsFetcher = statsFetcher;

		function statsFetcher() {
			return Project.taskStatistics(ctrl.project.shortName);
		}
	}

})();
