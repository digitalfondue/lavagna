(function () {
    'use strict';

    function supports_history_api() {
        return !!(window.history && history.pushState);
    }

    // horrible workaround
    // http://stackoverflow.com/questions/4508574/remove-hash-from-url seems an issue in angular+html5 mode
    // if we have a trailing # in html5mode it cause havoc :D
    if (window.location.href.indexOf('#') === (window.location.href.length - 1) && supports_history_api()) {
        window.history.pushState('', document.title, window.location.pathname);
    }

    window.csrfToken = window.document.documentElement.getAttribute('lavagna-csrf');

    // set momentjs to use the current locale
    var locale = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage);
    // moment.locale(locale);

    // declare all the modules here
    angular.module('lavagna.components', [ 'lavagna.services' ]);
    angular.module('lavagna.directives', [ 'lavagna.services' ]);
    angular.module('lavagna.filters', []);
    angular.module('lavagna.services', []);
    //

    var module = angular.module('lavagna', [ 'ui.router', 'lavagna.services',
        'lavagna.components', 'lavagna.filters', 'lavagna.directives',
        'ngSanitize', 'ngMessages',
        'pascalprecht.translate',
        'angularFileUpload', 'ngMaterial', 'materialCalendar']);

    module.constant('CONTEXT_PATH', document.getElementsByTagName('base')[0].href);
    module.constant('LOCALE_FIRST_DAY_OF_WEEK', document.getElementsByTagName('html')[0].getAttribute('data-lavagna-first-day-of-week'));

    // http://www.rosher.co.uk/post/2014/03/26/Angular-Tips-Measuring-Rendering-Performance.aspx
    /* module.run(['$rootScope', function($rootScope) {
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
    module.config(function ($stateProvider, $urlRouterProvider, $locationProvider, $translateProvider, $mdThemingProvider, $mdIconProvider, $mdInkRippleProvider, $compileProvider, $qProvider) {
        $compileProvider.debugInfoEnabled(false);
        $compileProvider.preAssignBindingsEnabled(true);

        // disable comment and css class directives for perf
        $compileProvider.commentDirectivesEnabled(false);
        $compileProvider.cssClassDirectivesEnabled(false);
        //

        // https://github.com/angular-ui/ui-router/issues/2889
        $qProvider.errorOnUnhandledRejections(false);
        //

        $locationProvider.html5Mode(true);

        // TODO: this is kinda fragile
        $urlRouterProvider.rule(function ($injector, $location) {
            // http://stackoverflow.com/questions/4508574/remove-hash-from-url seems an issue in angular+html5 mode
            // if we have a trailing # in html5mode it cause havoc :D
            if (window.location.href.indexOf('#') === (window.location.href.length - 1) && supports_history_api()) {
                window.history.pushState('', document.title, window.location.pathname);
            }

            var path = $location.url();
            var afterHash = '';

            if (path.indexOf('#') !== -1) {
                afterHash = path.substr(path.indexOf('#'));
                path = path.substr(0, path.indexOf('#'));
            }

            // exception: handle board URL.
            if (path.match(/^\/[A-Z0-9_]+\/[A-Z0-9_]+$/) || path.match(/^\/[A-Z0-9_]+\/[A-Z0-9_]+\?.*$/)) {
                return;
            }
            // exception: handle board URL. remove trailing slash
            if (path.match(/^\/[A-Z0-9_]+\/[A-Z0-9_]+\/$/)) {
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

        angular.forEach(io_lavagna.i18n, function (map, lang) {
            $translateProvider.translations(lang, map);
        });
        $translateProvider.preferredLanguage(locale);
        $translateProvider.fallbackLanguage('en');
        $translateProvider.usePostCompiling(true);
        $translateProvider.useSanitizeValueStrategy('escape');

        var cardCtrlResolver = {
            card: function (CardCache, $stateParams) {
                return CardCache.cardByBoardShortNameAndSeqNr($stateParams.shortName, $stateParams.seqNr);
            },
            project: function (ProjectCache, $stateParams) {
                return ProjectCache.project($stateParams.projectName);
            },
            board: function (BoardCache, $stateParams) {
                return BoardCache.board($stateParams.shortName);
            },
            user: function (User) {
                return User.currentCachedUser();
            },
            metadata: function (ProjectCache, $stateParams) {
                return ProjectCache.metadata($stateParams.projectName);
            }
        };

        var cardProjectResolver = {
            board: function (BoardCache, $stateParams) {
                return BoardCache.board($stateParams.shortName);
            },
            card: function (CardCache, $stateParams) {
                return CardCache.cardByBoardShortNameAndSeqNr($stateParams.shortName, $stateParams.seqNr);
            },
            metadata: function (ProjectCache, $stateParams) {
                return ProjectCache.metadata($stateParams.projectName);
            }
        };

        var cardResolver = {
            card: function (CardCache, $stateParams) {
                return CardCache.cardByBoardShortNameAndSeqNr($stateParams.shortName, $stateParams.seqNr);
            },
            metadata: function (ProjectCache, $stateParams) {
                return ProjectCache.metadata($stateParams.projectName);
            }
        };

        var projectResolver = {
            project: function (ProjectCache, $stateParams) {
                return ProjectCache.project($stateParams.projectName);
            },
            user: function (User) {
                return User.currentCachedUser();
            }
        };

        var boardResolver = {
            project: function (ProjectCache, $stateParams) {
                return ProjectCache.project($stateParams.projectName);
            },
            user: function (User) {
                return User.currentCachedUser();
            },
            board: function (BoardCache, $stateParams) {
                return BoardCache.board($stateParams.shortName);
            }
        };

        var userResolver = {
            user: function (User, $stateParams) {
                return User.getUserProfile($stateParams.provider, $stateParams.username, 0);
            },
            isCurrentUser: function (User, $stateParams) {
                return User.isCurrentUser($stateParams.provider, $stateParams.username);
            }
        };

        var currentUserResolver = {
            user: function (User) {
                return User.currentCachedUser();
            }
        };

        var titleServiceResolver = {
            Title: function (Title) {
                return Title;
            }
        };

        $stateProvider.state('home', {
            url: '/',
            abstract: true,
            template: '<md-toolbar><lvg-navbar-dashboard></lvg-navbar-dashboard></md-toolbar><div data-ui-view></div>',
            onEnter: function (Title) {
                Title.set('title.dashboard');
            }
        })
        .state('home.dashboard', {
            url: '',
            template: '<lvg-dashboard user="rslvr.user"></lvg-dashboard>',
            resolve: currentUserResolver,
            controller: function (user) {
                this.user = user;
            },
            controllerAs: 'rslvr',
        })
        // ---- ABOUT ----
        .state('about', {
            url: '/about/',
            abstract: true,
            template: '<lvg-navbar-about></lvg-navbar-about><div data-ui-view data-autoscroll="false" class="lvg-content"></div>',
            onEnter: function (Title) {
                Title.set('title.about');
            }
        })
        .state('about.lavagna', {
            url: '',
            template: '<lvg-about-license></lvg-about-license>'
        })
        .state('about.third-party', {
            url: 'third-party/',
            template: '<lvg-about-licenses></lvg-about-licenses>'
        })
        // ---- ACCOUNT ----
        .state('account', {
            url: '/me/',
            template: '<lvg-account username="rslvr.username" provider="rslvr.provider" is-current-user="rslvr.isCurrentUser"></lvg-account>',
            controller: function (user) {
                this.username = user.username;
                this.provider = user.provider;
                this.isCurrentUser = true;
            },
            controllerAs: 'rslvr',
            resolve: currentUserResolver,
            onEnter: function (Title) {
                Title.set('title.account');
            }
        }).state('calendar', {
            url: '/calendar/',
            template: '<lvg-navbar-basic title-text="title.calendar"></lvg-navbar-basic><lvg-calendar username="rslvr.username" provider="rslvr.provider"></lvg-calendar>',
            controller: function (user) {
                this.username = user.username;
                this.provider = user.provider;
            },
            controllerAs: 'rslvr',
            resolve: currentUserResolver,
            onEnter: function (Title) {
                Title.set('title.calendar');
            }
        }).state('calendar.card', {
            url: ':projectName/{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template: '<lvg-card-modal project="rslvr.project" board="rslvr.board" card="rslvr.card" user="rslvr.user"></lvg-card-modal>',
            controller: function (Title, card, project, board, user, metadata) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                project.metadata = metadata;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs: 'rslvr',
            resolve: cardCtrlResolver
        }).state('user', {
            url: '/user/:provider/:username/',
            abstract: true,
            template: '<lvg-navbar-user data-username="rslvr.username" data-provider="rslvr.provider" data-is-current-user="rslvr.isCurrentUser"> </lvg-navbar-user><div data-ui-view data-autoscroll="false"></div>',
            controller: function (user, isCurrentUser) {
                this.user = user;
                this.username = user.user.username;
                this.provider = user.user.provider;
                this.isCurrentUser = isCurrentUser;
            },
            controllerAs: 'rslvr',
            resolve: userResolver,
            onEnter: function (Title, user) {
                Title.set('title.user.profile', { username: user.user.username });
            }
        }).state('user.dashboard', {
            url: '',
            template: '<lvg-user-dashboard profile="rslvr.user"></lvg-user-dashboard>'
        }).state('user.projects', {
            url: 'projects/',
            template: '<lvg-user-projects profile="rslvr.user"></lvg-user-projects>'
        }).state('user.activity', {
            url: 'activity/',
            template: '<lvg-user-activity profile="rslvr.user"></lvg-user-activity>'
        })
        // ---- SEARCH ----
        .state('globalSearch', {
            url: '/search/?q&page',
            template: '<lvg-search user="rslvr.user"></lvg-search>',
            reloadOnSearch: false,
            controllerAs: 'rslvr',
            resolve: currentUserResolver,
            controller: function (Title, user) {
                Title.set('title.search');
                this.user = user;
            }
        }).state('globalSearch.card', {
            url: ':projectName/{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template: '<lvg-card-modal project="rslvr.project" board="rslvr.board" card="rslvr.card" user="rslvr.user"></lvg-card-modal>',
            controller: function (Title, card, project, board, user, metadata) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                project.metadata = metadata;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs: 'rslvr',
            resolve: cardCtrlResolver
        })
        // ---- ADMIN ----
        .state('admin', {
            url: '/admin/',
            abstract: true,
            template: '<lvg-admin></lvg-admin>',
            resolve: titleServiceResolver,
            onEnter: function (Title) {
                Title.set('title.admin');
            }
        }).state('admin.home', {
            url: '',
            template: '<lvg-admin-parameters></lvg-admin-parameters>'
        }).state('admin.roles', {
            url: 'roles/',
            template: '<lvg-manage-roles></lvg-manage-roles>'
        }).state('admin.exportimport', {
            url: 'export-import/',
            template: '<lvg-admin-export-import></lvg-admin-export-import>'
        }).state('admin.endpointinfo', {
            url: 'endpoint-info/',
            template: '<lvg-admin-endpoints></lvg-admin-endpoints>'
        }).state('admin.users', {
            url: 'users/',
            template: '<lvg-admin-users></lvg-admin-users>'
        }).state('admin.login', {
            url: 'login/',
            template: '<lvg-admin-login oauth-providers="rslvr.oauthProviders"></lvg-admin-login>',
            controller: function (oauthProviders) {
                this.oauthProviders = oauthProviders;
            },
            controllerAs: 'rslvr',
            resolve: {'oauthProviders': function (Admin) { return Admin.findAllOauthProvidersInfo(); }}
        }).state('admin.smtp', {
            url: 'smtp/',
            template: '<lvg-admin-smtp></lvg-admin-smtp>'
        }).state('admin.integrations', {
            url: 'integrations/',
            template: '<lvg-admin-integrations></lvg-admin-integrations>'
        })

        // ---- MANAGE PROJECT ----
        .state('projectManage', {
            url: '/:projectName/manage/',
            template: '<lvg-navbar-project-manage project="rslvr.project"></lvg-navbar-project-manage><div data-ui-view data-autoscroll="false"></div>',
            abstract: true,
            controller: function (project) {
                this.project = project;
            },
            controllerAs: 'rslvr',
            resolve: projectResolver,
            onEnter: function (Title, project) {
                Title.set('title.project.manage', { shortname: project.shortName });
            }
        }).state('projectManage.roles', {
            url: 'roles/',
            template: '<lvg-manage-roles project="rslvr.project"></lvg-manage-roles>'
        }).state('projectManage.import', {
            url: 'import/',
            template: '<lvg-project-manage-import project="rslvr.project"></lvg-project-manage-import>'
        }).state('projectManage.labels', {
            url: 'labels/',
            template: '<lvg-project-manage-labels project="rslvr.project"></lvg-project-manage-labels>'
        }).state('projectManage.milestones', {
            url: 'milestones/',
            template: '<lvg-project-manage-milestones project="rslvr.project"></lvg-project-manage-milestones>'
        }).state('projectManage.mailTicket', {
            url: 'mail-ticket/',
            template: '<lvg-project-manage-mail-ticket project="rslvr.project"></lvg-project-manage-mail-ticket>'
        }).state('projectManage.access', {
            url: 'access/',
            template: '<lvg-project-manage-access project="rslvr.project"></lvg-project-manage-access>'
        }).state('projectManage.boards', {
            url: 'boards/',
            template: '<lvg-project-manage-boards project="rslvr.project"></lvg-project-manage-boards>'
        }).state('projectManage.project', {
            url: '',
            template: '<lvg-project-manage-project project="rslvr.project"></lvg-project-manage-project>',
        })

        // ---- PROJECT ----
        .state('project', {
            abstract: true,
            url: '/:projectName/',
            controller: function (Title, project, user) {
                this.project = project;
                this.user = user;
                Title.set('title.project', { shortname: project.shortName, name: project.name });
            },
            controllerAs: 'rslvr',
            resolve: projectResolver,
            template: '<lvg-navbar-project project="rslvr.project"></lvg-navbar-project><div data-ui-view data-autoscroll="false"></div>'
        })
        .state('project.boards', {
            url: '',
            template: '<lvg-project-boards project="rslvr.project" user="rslvr.user"></lvg-project-boards>'
        }).state('project.statistics', {
            url: 'statistics/',
            template: '<lvg-project-statistics project="rslvr.project"></lvg-project-statistics>'
        }).state('project.calendar', {
            url: 'calendar/',
            template: '<lvg-calendar username="rslvr.username" provider="rslvr.provider" project="rslvr.project"></lvg-calendar>',
            controller: function (project, user) {
                this.username = user.username;
                this.provider = user.provider;
                this.project = project;
            },
            controllerAs: 'rslvr',
            resolve: projectResolver
        }).state('project.calendar.card', {
            url: '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template: '<lvg-card-modal project="rslvr.project" board="rslvr.board" card="rslvr.card" user="rslvr.user"></lvg-card-modal>',
            controller: function (Title, card, project, board, user, metadata) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                project.metadata = metadata;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs: 'rslvr',
            resolve: cardCtrlResolver
        }).state('project.milestones', {
            url: 'milestones/',
            template: '<lvg-project-milestones project="rslvr.project"></lvg-project-milestones>'
        }).state('project.milestones.milestone', {
            url: ':id/',
            template: '<lvg-project-milestone project="rslvr.project" id="rslvr.id"></lvg-project-milestone>',
            controllerAs: 'rslvr',
            controller: function (project, $stateParams) {
                this.project = project;
                this.id = $stateParams.id;
            },
            resolve: {project: function (ProjectCache, $stateParams) { return ProjectCache.project($stateParams.projectName); }}
        }).state('project.milestones.milestone.card', {
            url: '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template: '<lvg-card-modal project="rslvr.project" board="rslvr.board" card="rslvr.card" user="rslvr.user"></lvg-card-modal>',
            controller: function (Title, card, project, board, user, metadata) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                project.metadata = metadata;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs: 'rslvr',
            resolve: cardProjectResolver
        })

        // ---- PROJECT SEARCH ----
        .state('projectSearch', {
            url: '/:projectName/search/?q&page',
            template: '<lvg-search project="rslvr.project" user="rslvr.user"></lvg-search>',
            reloadOnSearch: false,
            controller: function (Title, project, user) {
                this.project = project;
                this.user = user;
                Title.set('title.project', { shortname: project.shortName, name: project.name });
            },
            controllerAs: 'rslvr',
            resolve: projectResolver
        }).state('projectSearch.card', {
            url: '{shortName:[A-Z0-9_]+}-{seqNr:[0-9]+}/',
            template: '<lvg-card-modal project="rslvr.project" board="rslvr.board" card="rslvr.card" user="rslvr.user"></lvg-card-modal>',
            controller: function (Title, card, project, board, user, metadata) {
                this.board = board;
                this.card = card;
                this.project = project;
                this.user = user;
                project.metadata = metadata;
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            controllerAs: 'rslvr',
            resolve: cardProjectResolver
        })

        // ---- BOARD ----
        .state('board', {
            url: '/:projectName/{shortName:[A-Z0-9_]+}?q',
            reloadOnSearch: false,
            template: '<lvg-board project="rslvr.project" board="rslvr.board" user-reference="::rslvr.user"></lvg-board>',
            controller: function (project, board, user) {
                this.project = project;
                this.board = board;
                this.user = user;
            },
            controllerAs: 'rslvr',
            resolve: boardResolver,
            onEnter: function (Title, project, board) {
                Title.set('title.board', { projectshortname: project.shortName, shortname: board.shortName, name: board.name });
            }
        })
        .state('board.card', {
            url: '-{seqNr:[0-9]+}/',
            template: '<lvg-card-modal project="rslvr.project" board="rslvr.board" card="rslvr.card" user="rslvr.user"></lvg-card-modal>',
            controller: function (card, project, board, user, metadata) {
                project.metadata = metadata;
                this.card = card;
                this.board = board;
                this.project = project;
                this.user = user;
            },
            controllerAs: 'rslvr',
            resolve: cardResolver,
            onEnter: function (Title, board, card) {
                Title.set('title.card', { shortname: board.shortName, sequence: card.sequence, name: card.name });
            },
            onExit: function (Title, card, project, board) {
                Title.set('title.board', { projectshortname: project.shortName, shortname: board.shortName, name: board.name });
            }
        });

        $urlRouterProvider.otherwise('/');

        if (true) { /* eslint no-constant-condition: 0 */
            $mdThemingProvider.disableTheming();
        } else {
            /* CHECK in case we need to regenerate the theme! https://github.com/angular/material/blob/master/docs/app/performance/internet-explorer.md#theming */
            var background = $mdThemingProvider.extendPalette('grey', {
                  // 'A100': 'e5e5e5'
                'A100': 'ffffff'
            });

            $mdThemingProvider.definePalette('lavagna-background', background);

            $mdThemingProvider.theme('default')
                .primaryPalette('blue-grey')
                .accentPalette('light-blue')
                .warnPalette('red')
                .backgroundPalette('lavagna-background');

            $mdThemingProvider.setDefaultTheme('default');
        }

        $mdInkRippleProvider.disableInkRipple();

        // FIXME use a svg icon set
        $mdIconProvider
            .icon('add', 'svg/ic_add_white_48px.svg')
            .icon('menu', 'svg/ic_menu_white_48px.svg')
            .icon('settings', 'svg/ic_settings_white_48px.svg')
            .icon('person', 'svg/ic_person_white_48px.svg')
            .icon('person_black', 'svg/ic_person_black_48px.svg')
            .icon('group_add_black', 'svg/ic_group_add_black_48px.svg')
            .icon('person_add_black', 'svg/ic_person_add_black_48px.svg')
            .icon('circle', 'svg/ic_circle_black_48px.svg')
            .icon('calendar', 'svg/calendar.svg')
            .icon('folder-move', 'svg/folder-move.svg')
            .icon('milestone', 'svg/ic_location_on_black_48px.svg')
            .icon('label', 'svg/ic_label_black_48px.svg')
            .icon('task', 'svg/ic_assignment_black_48px.svg')
            .icon('edit', 'svg/ic_mode_edit_black_48px.svg')
            .icon('archive', 'svg/ic_archive_black_48px.svg')
            .icon('unarchive', 'svg/ic_unarchive_black_48px.svg')
            .icon('close', 'svg/ic_close_black_48px.svg')
            .icon('error', 'svg/ic_error_outline_black_48px.svg')
            .icon('ok', 'svg/ic_check_black_48px.svg')
            .icon('expand_less', 'svg/ic_expand_less_black_48px.svg')
            .icon('expand_more', 'svg/ic_expand_more_black_48px.svg')
            .icon('info', 'svg/ic_info_black_48px.svg')
            .icon('help', 'svg/ic_help_black_48px.svg')
            .icon('search', 'svg/ic_search_black_48px.svg')
            .icon('delete', 'svg/ic_delete_black_48px.svg')
            .icon('drag', 'svg/drag.svg')
            .icon('comment', 'svg/ic_comment_black_48px.svg')
            .icon('file', 'svg/ic_insert_drive_file_black_48px.svg')
            .icon('list', 'svg/ic_list_black_48px.svg')
            .icon('clock', 'svg/ic_access_time_black_48px.svg')
            .icon('notifications', 'svg/ic_notifications_black_48px.svg')
            .icon('notifications-off', 'svg/ic_notifications_off_black_48px.svg')
            .icon('take', 'svg/ic_assignment_ind_black_48px.svg')
            .icon('surrender', 'svg/ic_assignment_returned_black_48px.svg')
            .icon('upload', 'svg/ic_cloud_upload_black_48px.svg')
            .icon('replay', 'svg/ic_replay_black_48px.svg')
            .icon('align_top', 'svg/ic_vertical_align_top_black_48px.svg')
            .icon('align_bottom', 'svg/ic_vertical_align_bottom_black_48px.svg')
            .icon('milestone_open', 'svg/milestone-open.svg')
            .icon('milestone_closed', 'svg/milestone-closed.svg')
            .icon('location_backlog', 'svg/ic_developer_board_black_48px.svg')
            .icon('location_archive', 'svg/archive.svg')
            .icon('location_trash', 'svg/trash.svg')
            .icon('dashboard', 'svg/ic_dashboard_black_48px.svg')
            .icon('dashboard_white', 'svg/ic_dashboard_white_48px.svg')
            .icon('menu_ellipsis', 'svg/ic_more_vert_black_48px.svg')
            .icon('menu_ellipsis_white', 'svg/ic_more_vert_white_48px.svg')
            .icon('add_column', 'svg/add_column.svg')
            .icon('select_all', 'svg/ic_select_all_black_48px.svg')
            .icon('add-to-list', 'svg/ic_playlist_add_black_48px.svg')
            .icon('chevron-left', 'svg/ic_chevron_left_black_48px.svg')
            .icon('chevron-right', 'svg/ic_chevron_right_black_48px.svg')
            .icon('file-excel', 'svg/file-excel.svg')
            .icon('logout', 'svg/logout.svg')
            .icon('repeat', 'svg/repeat.svg')
            .icon('repeat-off', 'svg/repeat-off.svg')
            .icon('security-key', 'svg/ic_vpn_key_black_48px.svg')
            .icon('sync', 'svg/ic_sync_black_48px.svg')
            .icon('sync_disabled', 'svg/ic_sync_disabled_black_48px.svg')
            .icon('sort-ascending', 'svg/sort-ascending.svg')
            .icon('sort-descending', 'svg/sort-descending.svg');
    });

    module.config(function ($mdDateLocaleProvider, LOCALE_FIRST_DAY_OF_WEEK) {
        // calendar conf, TODO: configurable
        var dateFormat = 'D.M.YYYY';

        $mdDateLocaleProvider.firstDayOfWeek = parseInt(LOCALE_FIRST_DAY_OF_WEEK);
        $mdDateLocaleProvider.parseDate = function (dateString) {
            if (!angular.isDefined(dateString) || dateString === null || dateString === '') {
                return null;
            }
            var m = moment(dateString, dateFormat, true);

            return m.isValid() ? m.toDate() : new Date(NaN);
        };
        $mdDateLocaleProvider.formatDate = function (date) {
            if (!angular.isDefined(date) || date === null) {
                return null;
            }

            return moment(date).format(dateFormat);
        };
        //
    });

    module.run(function ($rootScope, $state, $mdSidenav, $log) {
        $rootScope.$on('$stateChangeError', function (event, toState, toParams, fromState, fromParams, error) {
            event.preventDefault();
            $log.debug(error);
            // FIXME
        });

        $rootScope.$on('$stateChangeSuccess', function () {
            $mdSidenav('left').close();
        });
    });

    /**
     * install CSRF token filter.
     *
     * security filter set the CSRF token in the http header.
     *
     */

    module.factory('lavagnaHttpInterceptor', function ($q, $window) {
        //
        return {
            'request': function (config) {
                if (angular.isDefined($window.csrfToken) && $window.csrfToken !== null) {
                    config.headers['x-csrf-token'] = $window.csrfToken;
                }

                return config;
            },
            'response': function (response) {
                var headers = response.headers();

                if (headers['content-type'] && headers['content-type'].indexOf('application/json') === 0 && headers['x-csrf-token']) {
                    $window.csrfToken = response.headers()['x-csrf-token'];
                }

                return response;
            },
            'responseError': function (rejection) {
                // if the session has been lost, trigger a reload
                if (rejection.status === 401) {
                    $window.location.reload();
                }

                return $q.reject(rejection);
            }
        };
    });

    module.config(function ($httpProvider) {
        $httpProvider.interceptors.push('lavagnaHttpInterceptor');
        $httpProvider.useApplyAsync(true);
    });
}());
