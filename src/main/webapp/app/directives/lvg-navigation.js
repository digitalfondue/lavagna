(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgProjectNavigation', function () {
		return {
			restrict: 'E',
			template: '<a data-ui-sref="home"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'{{::projectResolver.project.shortName}}<span class="separator">-</span>{{projectResolver.project.name}}<a data-lvg-has-permission="PROJECT_ADMINISTRATION" class="project-settings-shortcut" data-ui-sref="ProjectManage.project({projectName: projectResolver.project.shortName})" title="{{\'partials.project.fragments.nabvar.projectAdmin\' | translate}}"><i class="fa fa-cog"></i></a>'
		}
	});

	directives.directive('lvgBoardNavigation', function () {
		return {
			restrict: 'E',
			template: '<a data-ui-sref="home"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'<a data-ui-sref="project.boards({projectName: boardCtrlResolver.project.shortName})">{{::boardCtrlResolver.project.shortName}}</a> <a data-lvg-has-permission="PROJECT_ADMINISTRATION" class="project-settings-shortcut" data-ui-sref="ProjectManage.project({projectName: boardCtrlResolver.project.shortName})" title="{{\'partials.project.fragments.nabvar.projectAdmin\' | translate}}"><i class="fa fa-cog"></i></a><i class="fa fa-angle-right"></i>' +
			'{{::board.shortName}}<span class="separator">-</span>{{board.name}}'
		}
	});

	directives.directive('lvgProjectSettingsNavigation', function () {
		return {
			restrict: 'E',
			template: '<a data-ui-sref="home"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'<a data-ui-sref="project.boards({projectName: projectResolver.project.shortName})">{{::projectResolver.project.shortName}}</a><i class="fa fa-angle-right"></i>' +
			'<span data-translate>partials.project.fragments.nabvar.admin</span>'
		}
	});

})();
