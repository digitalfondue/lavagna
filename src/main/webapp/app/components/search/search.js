(function() {

	'use strict';

	var components = angular.module('lavagna.components');

    components.component('lvgSearch', {
        bindings: {
            project: '=',
            user:'<'
        },
    	controller: SearchCtrl,
    	templateUrl: 'app/components/search/search.html'
    });


	function SearchCtrl($scope, $location, $http, $log, $filter, Search, User, LabelCache, Card) {
		var ctrl = this;

		function triggerSearch() {

			var searchParams = $location.search();

			ctrl.query= searchParams.q;
			ctrl.page = searchParams.page || 1;

			try {
				var r = Search.parse(searchParams.q);
				queryString.params.q = JSON.stringify(r);
				queryString.params.page = ctrl.page - 1;
				$http.get('api/search/card', queryString).then(function(res) {
					ctrl.found = res.data.found.slice(0, res.data.countPerPage);
					ctrl.count = res.data.count;
					ctrl.currentPage = res.data.currentPage+1;
					ctrl.countPerPage = res.data.countPerPage;
					ctrl.totalPages = res.data.totalPages;
					ctrl.pages = [];
					for(var i = 1; i<=res.data.totalPages;i++) {
						ctrl.pages.push(i);
					}
					$log.debug(ctrl.pages);
				});
			} catch(e) {
				$log.debug(e);
			}
		}

		var queryString = { params: {}};

		ctrl.moveToPage = function(page) {
			$log.debug('move to page', page);
			var loc = $location.search();
			loc.page = page;
			$location.search(loc);
			triggerSearch();
		};

		ctrl.selected = {};

		function selectedCardsCount() {
			var cnt = 0;
			for(var project in ctrl.selected) {
				for(var cardId in ctrl.selected[project]) {
					if(ctrl.selected[project][cardId]) {
						cnt++;
					}
				}
			}
			return cnt;
		}

		ctrl.selectedCardsCount = selectedCardsCount;

		ctrl.inProject = ctrl.project !== undefined;

		if(ctrl.project !== undefined) {
			queryString.params.projectName = ctrl.project.shortName;

			LabelCache.findByProjectShortName(ctrl.project.shortName).then(function(res) {

				ctrl.labels = res;
				for(var k in res) {
					if(res[k].domain === 'SYSTEM' && res[k].name === 'MILESTONE') {
						ctrl.milestoneLabel = res[k];
						break;
					}
				}
			});
		}

		ctrl.selectAllInPage = function() {

			var projects = {};

			for(var i = 0;i<ctrl.found.length;i++) {
				if(!projects[ctrl.found[i].projectShortName]) {
					projects[ctrl.found[i].projectShortName] = [];
				}
				projects[ctrl.found[i].projectShortName].push(ctrl.found[i].id);
			}

			/*the user can only select the cards where he has the MANAGE_LABEL_VALUE, which is a project level property (or global)*/
			for(var proj in projects) {
				User.hasPermission('MANAGE_LABEL_VALUE', proj).then((function(idsToSetAsTrue, shortProjectName) {
					return function() {
						for(var i = 0;i<idsToSetAsTrue.length;i++) {
							if(!ctrl.selected[shortProjectName]) {
								ctrl.selected[shortProjectName] = {};
							}
							ctrl.selected[shortProjectName][idsToSetAsTrue[i]] = true;
						}
						
						$scope.$broadcast('updatecheckbox');
					}
				})(projects[proj], proj));
			};
		};

		ctrl.deselectAllInPage = function() {
			for(var project in ctrl.selected) {
				for(var i = 0;i<ctrl.found.length;i++) {
					delete ctrl.selected[project][ctrl.found[i].id];
				}
			}
			$scope.$broadcast('updatecheckbox');
		};

		triggerSearch();

		$scope.$on('refreshSearch', function() {ctrl.selected = {}; triggerSearch();});

		function collectIdsByProject() {
			var res = {};

			for(var projectShortName in ctrl.selected) {
				for(var cardId in ctrl.selected[projectShortName]) {
					if(ctrl.selected[projectShortName][cardId]) {
						if(!res[projectShortName]) {
							res[projectShortName] = [];
						}
						res[projectShortName].push(parseInt(cardId, 10));
					}
				}
			}
			return res;
		}

		ctrl.collectIdsByProject = collectIdsByProject;

		ctrl.triggerSearch = triggerSearch;

		//
	}

})();
