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
    //moment.locale(locale);

	//declare all the modules here
	angular.module('lavagna.controllers', [ 'lavagna.services' ]);
	angular.module('lavagna.components', [ 'lavagna.services' ]);
	angular.module('lavagna.directives', [ 'lavagna.services' ]);
	angular.module('lavagna.filters', []);
	angular.module('lavagna.services', []);
	//

	var module = angular.module('lavagna', [ 'ui.router', 'lavagna.services',
			'lavagna.controllers', 'lavagna.components', 'lavagna.filters', 'lavagna.directives',
			'ngSanitize', 'ui.sortable', 'pasvaz.bindonce', 'ui.bootstrap',
			'pascalprecht.translate', 'digitalfondue.dftabmenu', 'digitalfondue.dfautocomplete',
			'angularFileUpload']);

	module.constant('CONTEXT_PATH', document.getElementsByTagName("base")[0].href);
	module.constant('LOCALE_FIRST_DAY_OF_WEEK', document.getElementsByTagName("html")[0].getAttribute('data-lavagna-first-day-of-week'));

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

		var boardResolver = {
            project : function(ProjectCache, $stateParams) {
                return ProjectCache.project($stateParams.projectName);
            },
            board : function(BoardCache, $stateParams) {
                return BoardCache.board($stateParams.shortName);
            }
		}

		var userResolver = {
		    user: function(User, $stateParams) {
		        return User.getUserProfile($stateParams.provider, $stateParams.username, 0);
		    },
		    isCurrentUser: function(User, $stateParams) {
		        return User.isCurrentUser($stateParams.provider, $stateParams.username);
		    }
		}

		var currentUserResolver = {
            user: function(User) {
                return User.currentCachedUser();
            }
		}

		$stateProvider.state('home', {
			url : '/',
			template : '<lvg-component-dashboard></lvg-component-dashboard>'
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
			template: '<lvg-component-account></lvg-component-account>',
			controller: function(user) {
                this.provider = user.provider;
                this.username = user.username;
                this.isCurrentUser = true;
            },
            controllerAs: 'userResolver',
            resolve : currentUserResolver
		}).state('user', {
            url :'/user/:provider/:username/',
            abstract: true,
            template: '<lvg-component-user user="userResolver.user"></lvg-component-user>',
            controller: function(user, isCurrentUser) {
                this.user = user;
                this.provider = user.user.provider;
                this.username = user.user.username;
                this.isCurrentUser = isCurrentUser;
            },
            controllerAs: 'userResolver',
            resolve : userResolver
		}).state('user.dashboard', {
            url :'',
            template : '<lvg-component-user-dashboard profile="userCtrl.profile"></lvg-component-user-dashboard>'
        }).state('user.projects', {
        	url :'projects/',
            templateUrl : 'app/components/user/projects/projects.html'
        }).state('user.activity', {
        	url :'activity/',
            templateUrl : 'app/components/user/activity/activity.html'
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
			controller: 'AdminConfigureLoginCtrl',
			resolve: {'oauthProviders' : function(Admin) {return Admin.findAllOauthProvidersInfo();}}
		}).state('admin.adminManageSmtpConfiguration', {
			url : 'manage-smtp-configuration/',
			templateUrl : 'partials/admin/manage-smtp-configuration.html',
			controller : 'AdminManageSmtpConfigurationCtrl'
		})

		//---- MANAGE PROJECT ----
		.state('ProjectManage', {
        	url : '/:projectName/manage/',
			templateUrl : 'app/components/project/manage/manage.html',
			abstract: true,
          	controller : function(project) {
                this.project = project;
          	},
          	controllerAs: 'projectResolver',
          	resolve: projectResolver
        }).state('ProjectManage.roles', {
			url : 'roles/',
			template: '<lvg-component-manage-roles project="projectResolver.project"></lvg-component-manage-roles>'
		}).state('ProjectManage.import', {
			url : 'import/',
			templateUrl : 'partials/project/manage-import.html',
			controller: 'ManageImportCtrl',
          	resolve: projectResolver
		}).state('ProjectManage.labels', {
			url : 'labels/',
			template : '<lvg-component-project-manage-labels project="projectResolver.project"></lvg-component-project-manage-labels>'
		}).state('ProjectManage.projectManageMilestones', {
			url : 'milestones/',
			template: '<lvg-component-project-manage-milestones project="projectResolver.project"></lvg-component-project-manage-milestones>'
		}).state('ProjectManage.access', {
			url : 'anonymous-users-access/',
			template: '<lvg-component-project-manage-access project="projectResolver.project"></lvg-component-project-manage-access>'
		}).state('ProjectManage.projectManageColumnsStatus', {
			url : 'status/',
			template: '<lvg-component-project-manage-status project="projectResolver.project"></lvg-component-project-manage-status>'
		}).state('ProjectManage.boards', {
			url : 'boards/',
			template : '<lvg-component-project-manage-boards project="projectResolver.project"></lvg-component-project-manage-boards>'
		}).state('ProjectManage.project', {
			url : '',
			template: '<lvg-component-project-manage project="projectResolver.project"></lvg-component-project-manage>'
		})

		//---- PROJECT ----
		.state('project', {
		    abstract: true,
		    url : '/:projectName/',
		    templateUrl: 'app/components/project/project.html',
		    controller: function(project) {
                this.project = project;
            },
            controllerAs: 'projectResolver',
            resolve : projectResolver
		})
		.state('project.boards', {
			url: '',
			template: '<lvg-component-project project="projectResolver.project"></lvg-component-project>'
		}).state('project.statistics', {
			url: 'statistics/',
			template: '<lvg-component-project-statistics project="projectResolver.project"></lvg-component-project-statistics>'
		}).state('project.milestones', {
            url : 'milestones/',
            template: '<lvg-component-project-milestones project="projectResolver.project"></lvg-component-project-milestones>'
        }).state('project.milestones.card', {
            url : '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template : '<lvg-component-card project="projectResolver.project" board="cardCtrlResolver.board" card="cardCtrlResolver.card" app="appCtrl"></lvg-component-card>',
            controller : function(card, project, board) {
                this.board = board;
                this.card = card;
            },
            controllerAs : 'cardCtrlResolver',
            resolve : cardCtrlResolver
        })

        //---- PROJECT SEARCH ----
        .state('project.search', {
			url : 'search/?q&page',
			templateUrl : 'partials/project/search.html',
			controller: 'SearchCtrl',
			reloadOnSearch: false
		}).state('project.search.card', {
			url : '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
			template : '<lvg-component-card project="projectResolver.project" board="cardCtrlResolver.board" card="cardCtrlResolver.card" app="appCtrl"></lvg-component-card>',
            controller : function(card, project, board) {
                this.board = board;
                this.card = card;
            },
            controllerAs : 'cardCtrlResolver',
            resolve : cardCtrlResolver
		})

		//---- BOARD ----
		.state('projectBoard', {
		    abstract : true,
            url : '/:projectName/{shortName:[A-Z0-9_]+}',
            templateUrl : 'app/components/board/board.html',
            controller : function(project, board) {
                this.project = project;
                this.board = board;
            },
            controllerAs: 'boardCtrlResolver',
            resolve : boardResolver
		})
		.state('projectBoard.board', {
			url : '',
			template : '<lvg-component-board project="boardCtrlResolver.project" board="boardCtrlResolver.board"></lvg-component-board>'
		}).state('projectBoard.board.card', {
			url : '-{seqNr:[0-9]+}/',
			template : '<lvg-component-card project="boardCtrlResolver.project" board="boardCtrlResolver.board" card="cardCtrlResolver.card" app="appCtrl"></lvg-component-card>',
			controller : function(card, project, board) {
			    this.card = card;
			},
			controllerAs : 'cardCtrlResolver',
			resolve : cardCtrlResolver
		});

		$urlRouterProvider.otherwise('/');
	});

	module.run(function($rootScope, $state) {
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
})()
