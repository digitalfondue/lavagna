(function() {

	'use strict';

	function supports_history_api() {
		return !!(window.history && history.pushState);
	}

	// horrible workaround
	// http://stackoverflow.com/questions/4508574/remove-hash-from-url seems an issue in angular+html5 mode
	// if we have a trailing # in html5mode it cause havoc :D
	if(window.location.href.indexOf('#') === (window.location.href.length-1) && supports_history_api()) {
		window.history.pushState("", document.title, window.location.pathname);
	}

    // set momentjs to use the current locale
    var locale = navigator.languages? navigator.languages[0] : (navigator.language || navigator.userLanguage);
    moment.locale(locale);

	//declare all the modules here
	angular.module('lavagna.controllers', [ 'lavagna.services' ]);
	angular.module('lavagna.directives', [ 'lavagna.services' ]);
	angular.module('lavagna.filters', []);
	angular.module('lavagna.services', []);
	//

	var module = angular.module('lavagna', [ 'ui.router', 'lavagna.services',
			'lavagna.controllers', 'lavagna.filters', 'lavagna.directives',
			'ngSanitize', 'ui.sortable', 'pasvaz.bindonce', 'ui.bootstrap',
			'pascalprecht.translate', 'digitalfondue.dftabmenu', 'digitalfondue.dfautocomplete',
			'angularFileUpload']);

	module.constant('CONTEXT_PATH', document.getElementsByTagName("base")[0].href);

	// http://www.rosher.co.uk/post/2014/03/26/Angular-Tips-Measuring-Rendering-Performance.aspx
	/*module.run(['$rootScope', function($rootScope) {
		var $oldDigest = $rootScope.$digest;
		var $newDigest = function() {
			console.time("$digest");
			$oldDigest.apply($rootScope);
			console.timeEnd("$digest");
		};
		$rootScope.$digest = $newDigest;
	}])*/

	/**
	 * Configure angular-ui-router here...
	 */
	module.config(function ($stateProvider, $urlRouterProvider, $locationProvider, $translateProvider) {

		$locationProvider.html5Mode(true);

		//TODO: this is kinda fragile
		$urlRouterProvider.rule(function ($injector, $location) {

			//http://stackoverflow.com/questions/4508574/remove-hash-from-url seems an issue in angular+html5 mode
			//if we have a trailing # in html5mode it cause havoc :D
			if(window.location.href.indexOf('#') === (window.location.href.length-1) && supports_history_api()) {
				window.history.pushState("", document.title, window.location.pathname);
			}

		    var path = $location.url();
		    var afterHash = '';

		    if(path.indexOf('#') !== -1) {
		    	afterHash = path.substr(path.indexOf('#'));
		    	path = path.substr(0, path.indexOf('#'));
		    }

		    //exception: handle board URL.
		    if(path.match(/^\/[A-Z0-9_]+\/[A-Z0-9_]+$/) || path.match(/^\/[A-Z0-9_]+\/[A-Z0-9_]+\?.*$/)) {
		    	return;
		    }
		    //exception: handle board URL. remove trailing slash
		    if(path.match(/^\/[A-Z0-9_]+\/[A-Z0-9_]+\/$/)) {
		    	return path.substr(0, path.length - 1);
		    }

		    // check to see if the path already has a slash where it should be
		    if (path[path.length - 1] === '/' || path.indexOf('/?') > -1) {
		        return;
		    }

		    if (path.indexOf('?') > -1) {
		        return path.replace('?', '/?');
		    }

		    return path + '/' + afterHash;
		});

		angular.forEach(io_lavagna.i18n, function(map, lang) {
			$translateProvider.translations(lang, map)
		});
		$translateProvider.preferredLanguage(locale);
		$translateProvider.fallbackLanguage('en');
		$translateProvider.usePostCompiling(true);


		var cardCtrlResolver = {
				card : function(CardCache, $stateParams) {
					return CardCache.cardByBoardShortNameAndSeqNr($stateParams.shortName, $stateParams.seqNr);
				},
				currentUser : function(User) {
					return User.currentCachedUser();
				},
				project : function(ProjectCache, $stateParams) {
					return ProjectCache.project($stateParams.projectName);
				},
				board : function(BoardCache, $stateParams) {
					return BoardCache.board($stateParams.shortName);
				}
		};

		var projectResolver = {
			project : function(ProjectCache, $stateParams) {
				return ProjectCache.project($stateParams.projectName);
			}
		};

		$stateProvider.state('home', {
			url : '/',
			templateUrl : 'partials/home.html',
			controller : 'HomeCtrl'
		})
		.state('404', {
			url : '/not-found/',
			templateUrl : 'partials/404.html',
			controller : 'ErrorCtrl'
		})
		.state('500', {
			url : '/error/',
			templateUrl : 'partials/500.html',
			controller : 'ErrorCtrl'
		})
		//---- ABOUT ----
		.state('about', {
			url:'/about/',
			templateUrl: 'partials/about.html',
			controller: 'AboutLicenseCtrl'
		})
		.state('about-third-party', {
			url:'/about/third-party/',
			templateUrl: 'partials/about.html',
			controller: 'AboutThirdPartyCtrl'
		})
		//---- ACCOUNT ----
		.state('account', {
			url :'/me/',
			templateUrl : 'partials/me.html',
			controller: 'AccountCtrl'
		}).state('user', {
            url :'/user/:provider/:username/',
            templateUrl : 'partials/user.html',
            controller: 'UserCtrl'
        }).state('user-projects', {
        	url :'/user/:provider/:username/projects/',
            templateUrl : 'partials/user/projects.html',
            controller: 'UserProjectsCtrl'
        }).state('user-activity', {
        	url :'/user/:provider/:username/activity/',
            templateUrl : 'partials/user/activity.html',
            controller: 'UserActivityCtrl'
        })
		//---- SEARCH ----
		.state('globalSearch', {
			url : '/search/?q&page',
			templateUrl : 'partials/search.html',
			controller: 'SearchCtrl',
			reloadOnSearch: false
		}).state('globalSearch.card', {
			url : ':projectName/{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
			templateUrl : 'partials/card.html',
			controller : 'CardCtrl',
			resolve : cardCtrlResolver
		})
		//---- ADMIN ----
		.state('admin', {
			url: '/admin/',
			abstract: true,
			templateUrl : 'partials/admin/admin.html',
			controller: 'AdminCtrl'
		}).state('admin.adminShowAllParameters', {
			url: '',
			templateUrl: 'partials/admin/parameters.html',
			controller: 'AdminParametersCtrl'
		}).state('admin.adminRole', {
			url: 'role/',
			templateUrl : 'partials/fragments/common-manage-roles.html',
			controller: 'ManageRoleCtrl'
		}).state('admin.adminExportImport', {
			url: 'export-import/',
			templateUrl : 'partials/admin/export-import.html',
			controller : 'AdminExportImportCtrl'
		}).state('admin.adminEndpointInfo', {
			url: 'endpoint-info/',
			templateUrl : 'partials/admin/endpoint-info.html',
			controller: 'AdminEndpointInfoCtrl'
		}).state('admin.adminManageUsers', {
			url: 'manage-users/',
			templateUrl : 'partials/admin/manage-users.html',
			controller: 'AdminManageUsersCtrl'
		}).state('admin.adminConfigureLogin', {
			url: 'configure-login/',
			templateUrl: 'partials/admin/configure-login.html',
			controller: 'AdminConfigureLoginCtrl'
		}).state('admin.adminManageSmtpConfiguration', {
			url : 'manage-smtp-configuration/',
			templateUrl : 'partials/admin/manage-smtp-configuration.html',
			controller : 'AdminManageSmtpConfigurationCtrl'
		})

		//---- MANAGE PROJECT ----
		.state('ProjectManage', {
        	url : '/:projectName/manage/',
			templateUrl : 'partials/project/manage.html',
			abstract: true,
          	controller : 'ProjectManageCtrl',
          	resolve: projectResolver
        }).state('ProjectManage.projectRole', {
			url : 'roles/',
			templateUrl : 'partials/project/manage-roles.html',
			controller: 'ManageRoleCtrl'
		}).state('ProjectManage.projectImport', {
			url : 'import/',
			templateUrl : 'partials/project/manage-import.html',
			controller: 'ManageImportCtrl',
          	resolve: projectResolver
		}).state('ProjectManage.projectManageLabels', {
			url : 'labels/',
			templateUrl : 'partials/project/manage-labels.html',
			controller : 'ProjectManageLabelsCtrl',
          	resolve: projectResolver
		}).state('ProjectManage.projectManageMilestones', {
			url : 'milestones/',
			templateUrl : 'partials/project/manage-milestones.html',
			controller : 'ProjectManageMilestonesCtrl',
          	resolve: projectResolver
		}).state('ProjectManage.projectManageAnonymousUsers', {
			url : 'anonymous-users-access/',
			templateUrl : 'partials/project/manage-anonymous-users-access.html',
			controller : 'ProjectManageAnonymousUsersAccessCtrl',
          	resolve: projectResolver
		}).state('ProjectManage.projectManageColumnsStatus', {
			url : 'status/',
			templateUrl : 'partials/project/manage-columns-status.html',
			controller : 'ProjectManageColumnsStatusCtrl',
			resolve: projectResolver
		}).state('ProjectManage.projectManageBoards', {
			url : 'boards/',
			templateUrl : 'partials/project/manage-boards.html',
			controller : 'ProjectManageBoardsCtrl',
			resolve: projectResolver
		}).state('ProjectManage.projectManageHome', {
			url : '',
			templateUrl : 'partials/project/manage-home.html',
			controller : 'ProjectManageHomeCtrl',
			resolve: projectResolver
		})

		// ---- MILESTONES ----
		.state('projectMilestones', {
			url : '/:projectName/milestones/',
			templateUrl : 'partials/project/milestones.html',
			controller : 'ProjectMilestonesCtrl',
			resolve : projectResolver
		}).state('projectMilestones.card', {
			url : '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
			templateUrl : 'partials/card.html',
			controller : 'CardCtrl',
			resolve : cardCtrlResolver
		})


		//---- PROJECT ----
		.state('project', {
			url : '/:projectName/',
			templateUrl : 'partials/project/project.html',
			controller : 'ProjectCtrl',
			resolve : projectResolver
		}).state('projectStatistics', {
			url : '/:projectName/statistics/',
			templateUrl : 'partials/project/statistics.html',
			controller: 'ProjectStatisticsCtrl',
			resolve : projectResolver
		}).state('projectSearch', {
			url : '/:projectName/search/?q&page',
			templateUrl : 'partials/project/search.html',
			controller: 'SearchCtrl',
			reloadOnSearch: false
		}).state('projectSearch.card', {
			url : '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
			templateUrl : 'partials/card.html',
			controller : 'CardCtrl',
			resolve : cardCtrlResolver
		}).state('projectBoard', {
			url : '/:projectName/{shortName:[A-Z0-9_]+}',
			templateUrl : 'partials/board.html',
			controller : 'BoardCtrl',
			resolve : {
				project : function(ProjectCache, $stateParams) {
					return ProjectCache.project($stateParams.projectName);
				},
				board : function(BoardCache, $stateParams) {
					return BoardCache.board($stateParams.shortName);
				}
			}
		}).state('projectBoard.card', {
			url : '-{seqNr:[0-9]+}/',
			templateUrl : 'partials/card.html',
			controller : 'CardCtrl',
			resolve : cardCtrlResolver
		});

		$urlRouterProvider.otherwise('/');
	});

	//reset the title to "Lavagna" (default). The controller will override with his own value if necessary.
	module.run(function($rootScope, $state) {
		$rootScope.$on("$stateChangeSuccess", function(event, toState, toParams, fromState, fromParams){
			$rootScope.pageTitle = 'Lavagna'
		});

		$rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) {
		    event.preventDefault();
		    $state.go(error.status.toString());
		});
	})

	/**
	 * install CSRF token filter.
	 *
	 * security filter set the CSRF token in the http header.
	 *
	 */

	module.factory('lavagnaHttpInterceptor', function($q, $window) {
		var csrfToken = null;
		return {
			'request' : function(config) {
				if (csrfToken != null) {
					config.headers['x-csrf-token'] = csrfToken;
				}
				return config;
			},
			'response' : function(response) {
				var headers = response.headers();
				if(headers['content-type'] && headers['content-type'].indexOf('application/json') === 0 && headers['x-csrf-token']) {
					csrfToken = response.headers()['x-csrf-token'];
					$window.csrfToken = csrfToken;
				}
				return response;
			},
			'responseError': function(rejection) {
				//if the session has been lost, trigger a reload
				if(rejection.status === 401) {
					$window.location.reload();
				}
				return $q.reject(rejection);
			}
		};
	});

	module.config(function($httpProvider) {
		$httpProvider.interceptors.push('lavagnaHttpInterceptor');
	});
})();
