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
	angular.module('lavagna.components', [ 'lavagna.services' ]);
	angular.module('lavagna.directives', [ 'lavagna.services' ]);
	angular.module('lavagna.filters', []);
	angular.module('lavagna.services', []);
	//

	var module = angular.module('lavagna', [ 'ui.router', 'lavagna.services',
			'lavagna.components', 'lavagna.filters', 'lavagna.directives',
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
				},
				user: function(User) {
					return User.currentCachedUser();
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
		};

		var userResolver = {
		    user: function(User, $stateParams) {
		        return User.getUserProfile($stateParams.provider, $stateParams.username, 0);
		    },
		    isCurrentUser: function(User, $stateParams) {
		        return User.isCurrentUser($stateParams.provider, $stateParams.username);
		    }
		};

		var currentUserResolver = {
            user: function(User) {
                return User.currentCachedUser();
            }
		};

		var titleServiceResolver = {
				Title : function(Title) {
					return Title;
				}
		};

		$stateProvider.state('home', {
			url : '/',
			abstract: true,
			templateUrl: 'app/components/home/home.html',
			onEnter: function(Title) {
				Title.set('title.dashboard');
			}
		})
		.state('home.dashboard', {
		    url: '',
		    template: '<lvg-component-dashboard></lvg-component-dashboard>',
		})
		.state('404', {
			url : '/not-found/',
			templateUrl : 'partials/404.html',
			controller: function(Title) {
                Title.set('title.notfound');
            }
		})
		.state('500', {
			url : '/error/',
			templateUrl : 'partials/500.html',
			controller: function(Title) {
                Title.set('title.error');
            }
		})
		//---- ABOUT ----
		.state('about', {
			url:'/about/',
			abstract: true,
			templateUrl: 'app/components/about/about.html',
            onEnter: function(Title) {
                Title.set('title.about');
            }
		})
		.state('about.lavagna', {
            url: '',
            template: '<lvg-about-license></lvg-about-license>'
		})
		.state('about.third-party', {
			url:'third-party/',
			template: '<lvg-about-licenses></lvg-about-licenses>'
		})
		//---- ACCOUNT ----
		.state('account', {
			url :'/me/',
			templateUrl: 'app/components/account/account-template.html',
			controller: function(user) {
                this.user = user;
                this.isCurrentUser = true;
            },
            controllerAs: 'accountResolver',
            resolve : currentUserResolver,
            onEnter: function(Title) {
            	Title.set('title.account');
            }
		}).state('user', {
            url :'/user/:provider/:username/',
            abstract: true,
            templateUrl: 'app/components/user/user.html',
            controller: function(user, isCurrentUser) {
                this.user = user;
                this.isCurrentUser = isCurrentUser;
            },
            controllerAs: 'userResolver',
            resolve : userResolver,
            onEnter: function(Title, user) {
            	Title.set('title.user.profile', { username: user.user.username });
            }
		}).state('user.dashboard', {
            url :'',
            template : '<lvg-component-user-dashboard profile="userResolver.user"></lvg-component-user-dashboard>'
        }).state('user.projects', {
        	url :'projects/',
            template : '<lvg-component-user-projects profile="userResolver.user"></lvg-component-user-projects>'
        }).state('user.activity', {
        	url :'activity/',
            template : '<lvg-component-user-activity profile="userResolver.user"></lvg-component-user-activity>'
        })
		//---- SEARCH ----
		.state('globalSearch', {
			url : '/search/?q&page',
			template : '<lvg-search></lvg-search>',
			reloadOnSearch: false
		}).state('globalSearch.card', {
			url : ':projectName/{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
			template : '<lvg-component-card project="cardCtrlResolver.project" board="cardCtrlResolver.board" card="cardCtrlResolver.card" user="cardCtrlResolver.user"></lvg-component-card>',
            controller : function(Title, card, project, board, user) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs : 'cardCtrlResolver',
            resolve : cardCtrlResolver
		})
		//---- ADMIN ----
		.state('admin', {
			url: '/admin/',
			abstract: true,
			template : '<lvg-component-admin></lvg-component-admin>',
			resolve: titleServiceResolver,
			onEnter: function(Title) {
				Title.set('title.admin.home');
			}
		}).state('admin.home', {
			url: '',
			template: '<lvg-component-admin-parameters></lvg-component-admin-parameters>'
		}).state('admin.roles', {
			url: 'roles/',
			template: '<lvg-component-manage-roles></lvg-component-manage-roles>'
		}).state('admin.exportimport', {
			url: 'export-import/',
			template : '<lvg-component-admin-export-import></lvg-component-admin-export-import>'
		}).state('admin.endpointinfo', {
			url: 'endpoint-info/',
			template : '<lvg-component-admin-endpoints></lvg-component-admin-endpoints>'
		}).state('admin.users', {
			url: 'users/',
			template : '<lvg-component-admin-users></lvg-component-admin-users>'
		}).state('admin.login', {
			url: 'login/',
			template: '<lvg-component-admin-login oauth-providers="adminLoginRslvr.oauthProviders"></lvg-component-admin-login>',
			controller: function(oauthProviders) {
			    this.oauthProviders = oauthProviders;
			},
			controllerAs: 'adminLoginRslvr',
			resolve: {'oauthProviders' : function(Admin) {return Admin.findAllOauthProvidersInfo();}}
		}).state('admin.smtp', {
			url : 'smtp/',
			template : '<lvg-component-admin-smtp></lvg-component-admin-smtp>'
		})

		//---- MANAGE PROJECT ----
		.state('ProjectManage', {
        	url : '/:projectName/manage/',
			templateUrl : '/app/components/project/manage/manage.html',
			abstract: true,
          	controller : function(project) {
                this.project = project;
          	},
          	controllerAs: 'projectResolver',
          	resolve: projectResolver,
          	onEnter:function(Title, project) {
          		Title.set('title.project.manage', { shortname: project.shortName });
          	}
        }).state('ProjectManage.roles', {
			url : 'roles/',
			template: '<lvg-component-manage-roles project="projectResolver.project"></lvg-component-manage-roles>'
		}).state('ProjectManage.import', {
			url : 'import/',
			template : '<lvg-component-project-manage-import project="projectResolver.project"></lvg-component-project-manage-import>'
		}).state('ProjectManage.labels', {
			url : 'labels/',
			template : '<lvg-component-project-manage-labels project="projectResolver.project"></lvg-component-project-manage-labels>'
		}).state('ProjectManage.milestones', {
			url : 'milestones/',
			template: '<lvg-component-project-manage-milestones project="projectResolver.project"></lvg-component-project-manage-milestones>'
		}).state('ProjectManage.access', {
			url : 'access/',
			template: '<lvg-component-project-manage-access project="projectResolver.project"></lvg-component-project-manage-access>'
		}).state('ProjectManage.status', {
			url : 'status/',
			template: '<lvg-component-project-manage-status project="projectResolver.project"></lvg-component-project-manage-status>'
		}).state('ProjectManage.boards', {
			url : 'boards/',
			template : '<lvg-component-project-manage-boards project="projectResolver.project"></lvg-component-project-manage-boards>'
		}).state('ProjectManage.project', {
			url : '',
			template: '<lvg-component-project-manage-project project="projectResolver.project"></lvg-component-project-manage-project>',

		})

		//---- PROJECT ----
		.state('project', {
		    abstract: true,
		    url : '/:projectName/',
		    controller: function(Title, project) {
                this.project = project;
                Title.set('title.project', { shortname: project.shortName, name: project.name });
            },
            controllerAs: 'projectResolver',
            resolve : projectResolver,
            templateUrl: 'app/components/project/project.html'
		})
		.state('project.boards', {
			url: '',
			template: '<lvg-component-project-boards project="projectResolver.project"></lvg-component-project-boards>'
		}).state('project.statistics', {
			url: 'statistics/',
			template: '<lvg-component-project-statistics project="projectResolver.project"></lvg-component-project-statistics>'
		}).state('project.milestones', {
            url : 'milestones/',
            template: '<lvg-component-project-milestones project="projectResolver.project"></lvg-component-project-milestones>'
        }).state('project.milestones.card', {
            url : '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template : '<lvg-component-card project="cardCtrlResolver.project" board="cardCtrlResolver.board" card="cardCtrlResolver.card" user="cardCtrlResolver.user"></lvg-component-card>',
            controller : function(Title, card, project, board, user) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs : 'cardCtrlResolver',
            resolve : cardCtrlResolver
        })

        //---- PROJECT SEARCH ----
        .state('projectSearch', {
			url : '/:projectName/search/?q&page',
			template : '<lvg-search></lvg-search>',
			reloadOnSearch: false
		}).state('projectSearch.card', {
			url : '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
			template : '<lvg-component-card project="cardCtrlResolver.project" board="cardCtrlResolver.board" card="cardCtrlResolver.card" user="cardCtrlResolver.user"></lvg-component-card>',
            controller : function(Title, card, project, board, user) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs : 'cardCtrlResolver',
            resolve : cardCtrlResolver
		})

		//---- BOARD ----
		.state('board', {
            url : '/:projectName/{shortName:[A-Z0-9_]+}',
            template : '<lvg-component-board project="boardCtrlResolver.project" board="boardCtrlResolver.board"></lvg-component-board>',
            controller : function(project, board) {
                this.project = project;
                this.board = board;
            },
            controllerAs: 'boardCtrlResolver',
            resolve : boardResolver,
            onEnter: function(Title, project, board) {
            	Title.set('title.board', { projectshortname: project.shortName, shortname: board.shortName, name: board.name });
            }
		})
		.state('board.card', {
			url : '-{seqNr:[0-9]+}/',
			template : '<lvg-component-card project="cardCtrlResolver.project" board="cardCtrlResolver.board" card="cardCtrlResolver.card" user="cardCtrlResolver.user"></lvg-component-card>',
			controller : function(card, project, board, user) {
			    this.card = card;
			    this.board = board;
			    this.project = project;
			    this.user = user;
			},
			controllerAs : 'cardCtrlResolver',
			resolve : cardCtrlResolver,
			onEnter: function(Title, board, card) {
				Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
			},
            onExit: function(Title, card, project, board) {
                Title.set('title.board', { projectshortname: project.shortName, shortname: board.shortName, name: board.name });
            }
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
