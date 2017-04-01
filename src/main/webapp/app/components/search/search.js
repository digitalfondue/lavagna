(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgSearch', {
        bindings: {
            project: '<',
            user: '<'
        },
        controller: ['$location', '$http', '$log', '$filter', 'Search', 'User', 'LabelCache', 'Card', 'EventBus', SearchController],
        templateUrl: 'app/components/search/search.html'
    });

    function SearchController($location, $http, $log, $filter, Search, User, LabelCache, Card, EventBus) {
        var ctrl = this;

        //
        ctrl.selectedCardsCount = selectedCardsCount;
        ctrl.collectIdsByProject = collectIdsByProject;
        ctrl.triggerSearch = triggerSearch;
        ctrl.deselectAllInPage = deselectAllInPage;
        ctrl.selectAllInPage = selectAllInPage;
        ctrl.moveToPage = moveToPage;
        //

        var refreshSearchSub = angular.noop;

        ctrl.$onInit = function init() {
            ctrl.queryString = { params: {}};
            ctrl.selected = {};
            ctrl.inProject = ctrl.project !== undefined;

            if (ctrl.project !== undefined) {
                ctrl.queryString.params.projectName = ctrl.project.shortName;

                LabelCache.findByProjectShortName(ctrl.project.shortName).then(function (res) {
                    ctrl.labels = res;
                    for (var k in res) {
                        if (res[k].domain === 'SYSTEM' && res[k].name === 'MILESTONE') {
                            ctrl.milestoneLabel = res[k];
                            break;
                        }
                    }
                });
            }
            triggerSearch();
            refreshSearchSub = EventBus.on('refreshSearch', function () { ctrl.selected = {}; triggerSearch(); });
        };

        ctrl.$onDestroy = function onDestroy() {
            refreshSearchSub();
        };

        //

        function triggerSearch() {
            var searchParams = $location.search();

            ctrl.query = searchParams.q;
            ctrl.page = searchParams.page || 1;

            try {
                var r = Search.parse(searchParams.q);

                ctrl.queryString.params.q = JSON.stringify(r);
                ctrl.queryString.params.page = ctrl.page - 1;
                $http.get('api/search/card', ctrl.queryString).then(function (res) {
                    ctrl.found = res.data.found.slice(0, res.data.countPerPage);
                    ctrl.count = res.data.count;
                    ctrl.currentPage = res.data.currentPage + 1;
                    ctrl.countPerPage = res.data.countPerPage;
                    ctrl.totalPages = res.data.totalPages;
                    ctrl.pages = [];
                    for (var i = 1; i <= res.data.totalPages;i++) {
                        ctrl.pages.push(i);
                    }
                });
            } catch (e) {
                $log.debug(e);
            }
        }

        function moveToPage(page) {
            var loc = $location.search();

            loc.page = page;
            $location.search(loc);
            triggerSearch();
        }

        function selectedCardsCount() {
            var cnt = 0;

            for (var project in ctrl.selected) {
                for (var cardId in ctrl.selected[project]) {
                    if (ctrl.selected[project][cardId]) {
                        cnt++;
                    }
                }
            }

            return cnt;
        }

        function selectAllInPage() {
            var projects = {};

            for (var i = 0;i < ctrl.found.length;i++) {
                if (!projects[ctrl.found[i].projectShortName]) {
                    projects[ctrl.found[i].projectShortName] = [];
                }
                projects[ctrl.found[i].projectShortName].push(ctrl.found[i].id);
            }

            /* the user can only select the cards where he has the MANAGE_LABEL_VALUE, which is a project level property (or global)*/
            for (var proj in projects) {
                User.hasPermission('MANAGE_LABEL_VALUE', proj).then((function (idsToSetAsTrue, shortProjectName) {
                    return function () {
                        for (var i = 0;i < idsToSetAsTrue.length;i++) {
                            if (!ctrl.selected[shortProjectName]) {
                                ctrl.selected[shortProjectName] = {};
                            }
                            ctrl.selected[shortProjectName][idsToSetAsTrue[i]] = true;
                        }

                        EventBus.emit('updatecheckbox');
                    };
                })(projects[proj], proj));
            }
        }

        function deselectAllInPage() {
            for (var project in ctrl.selected) {
                for (var i = 0;i < ctrl.found.length;i++) {
                    delete ctrl.selected[project][ctrl.found[i].id];
                }
            }
            EventBus.emit('updatecheckbox');
        }

        function collectIdsByProject() {
            var res = {};

            for (var projectShortName in ctrl.selected) {
                for (var cardId in ctrl.selected[projectShortName]) {
                    if (ctrl.selected[projectShortName][cardId]) {
                        if (!res[projectShortName]) {
                            res[projectShortName] = [];
                        }
                        res[projectShortName].push(parseInt(cardId, 10));
                    }
                }
            }

            return res;
        }
        //
    }
}());
