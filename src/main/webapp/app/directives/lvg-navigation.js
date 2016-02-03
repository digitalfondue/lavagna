(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgBoardNavigation', function () {
		return {
			restrict: 'E',
			template: '<a data-ui-sref="home"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'<a data-ui-sref="project.boards({projectName: boardCtrl.project.shortName})">{{::boardCtrl.project.shortName}}</a> <a data-lvg-has-permission="PROJECT_ADMINISTRATION" class="project-settings-shortcut" data-ui-sref="projectManage.project({projectName: boardCtrl.project.shortName})" title="{{\'partials.project.fragments.nabvar.projectAdmin\' | translate}}"><i class="fa fa-cog"></i></a><i class="fa fa-angle-right"></i>' +
			'{{::boardCtrl.board.shortName}}<span class="separator">-</span>{{boardCtrl.board.name}}'
		}
	});

})();
