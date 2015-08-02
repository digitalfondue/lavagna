(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgProjectNavigation', function () {
		return {
			restrict: 'E',
			template: '<a ng-href="#"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'{{project.shortName}}<span class="separator">-</span>{{project.name}}<a data-lvg-has-permission="PROJECT_ADMINISTRATION" class="project-settings-shortcut" ng-href="#/{{project.shortName}}/manage/" title="{{\'partials.project.fragments.nabvar.projectAdmin\' | translate}}"><i class="fa fa-cog"></i></a>'
		}
	});

	directives.directive('lvgBoardNavigation', function () {
		return {
			restrict: 'E',
			template: '<a ng-href="#"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'<a ng-href="#/{{project.shortName}}/">{{project.shortName}}</a> <a data-lvg-has-permission="PROJECT_ADMINISTRATION" class="project-settings-shortcut" ng-href="#/{{project.shortName}}/manage/" title="{{\'partials.project.fragments.nabvar.projectAdmin\' | translate}}"><i class="fa fa-cog"></i></a><i class="fa fa-angle-right"></i>' +
			'{{board.shortName}}<span class="separator">-</span>{{board.name}}'
		}
	});

	directives.directive('lvgProjectSettingsNavigation', function () {
		return {
			restrict: 'E',
			template: '<a ng-href="#"><span data-translate>partials.home.title</span></a><i class="fa fa-angle-right"></i>' +
			'<a ng-href="#/{{project.shortName}}/">{{project.shortName}}</a><i class="fa fa-angle-right"></i>' +
			'<span data-translate>partials.project.fragments.nabvar.admin</span>'
		}
	});

})();