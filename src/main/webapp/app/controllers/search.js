(function() {

	'use strict';

	var module = angular.module('lavagna.controllers');

	module.controller('SearchCtrl', function($scope, $stateParams, $location, $http, $log, $filter, $modal, ProjectCache, Search, User, LabelCache, Card) {

		function triggerSearch() {

			var searchParams = $location.search();

			$scope.query= encodeURIComponent(searchParams.q);
			$scope.page = searchParams.page || 1;

			try {
				var r = Search.parse(searchParams.q);
				queryString.params.q = JSON.stringify(r);
				queryString.params.page = $scope.page - 1;
				$http.get('api/search/card', queryString).then(function(res) {
					$scope.found = res.data.found.slice(0, res.data.countPerPage);
					$scope.count = res.data.count;
					$scope.currentPage = res.data.currentPage+1;
					$scope.countPerPage = res.data.countPerPage;
					$scope.totalPages = res.data.totalPages;
					$scope.pages = [];
					for(var i = 1; i<=res.data.totalPages;i++) {
						$scope.pages.push(i);
					}
					$log.debug($scope.pages);
				});
			} catch(e) {
				$log.debug(e);
			}
		}

		var queryString = { params: {}};

		$scope.selected = {};

		$scope.moveToPage = function(page) {
			$log.debug('move to page', page);
			var loc = $location.search();
			loc.page = page;
			$location.search(loc);
			triggerSearch();
		};


		function updateSelectCount() {
			var cnt = 0;
			for(var k in $scope.selected) {
				if($scope.selected[k] !== false) {
					cnt++;
				}
			}
			$scope.selectCount = cnt;
		}

		$scope.updateSelectCount = updateSelectCount;
		$scope.inProject = $stateParams.projectName !== undefined;


		if($stateParams.projectName !== undefined) {
			queryString.params.projectName = $stateParams.projectName;

			ProjectCache.project($stateParams.projectName).then(function(p) {
				$scope.project = p;
			});

			LabelCache.findByProjectShortName($stateParams.projectName).then(function(res) {

				$scope.labels = res;
				for(var k in res) {
					if(res[k].domain === 'SYSTEM' && res[k].name === 'MILESTONE') {
						$scope.milestoneLabel = res[k];
						break;
					}
				}
			});
		}

		$scope.selectAllInPage = function() {

			var projects = {};

			for(var i = 0;i<$scope.found.length;i++) {
				if(!projects[$scope.found[i].projectShortName]) {
					projects[$scope.found[i].projectShortName] = [];
				}
				projects[$scope.found[i].projectShortName].push($scope.found[i].id);
			}

			/*the user can only select the cards where he has the MANAGE_LABEL_VALUE, which is a project level property (or global)*/
			for(var proj in projects) {
				User.hasPermission('MANAGE_LABEL_VALUE', proj).then((function(idsToSetAsTrue, shortProjectName) {
					return function() {
						for(var i = 0;i<idsToSetAsTrue.length;i++) {
							$scope.selected[idsToSetAsTrue[i]] = shortProjectName;
						}
						updateSelectCount();
					}
				})(projects[proj], proj));
			}
		};

		$scope.deselectAllInPage = function() {
			for(var i = 0;i<$scope.found.length;i++) {
				delete $scope.selected[$scope.found[i].id];
			}
			updateSelectCount();
		};

		triggerSearch();

		$scope.$on('refreshSearch', function() {$scope.selected = {}; triggerSearch();});

		var collectIdsByProject = function() {
			var res = {};

			for(var k in $scope.selected) {
				var projectShortName = $scope.selected[k];
				if(projectShortName !== false) {
					if(!(projectShortName in res)) {
						res[projectShortName] = [];
					}
					res[projectShortName].push(k);
				}
			}
			return res;
		}
		
		$scope.collectIdsByProject = collectIdsByProject;
		
		$scope.triggerSearch = triggerSearch;
		
		//
		User.currentCachedUser().then(function(currentUser) {
			$scope.currentUserId = currentUser.id;
		});
		//
		
		// dependencies for card fragment
        $scope.cardFragmentDependencies = {};
        var cardFragmentDependenciesToCopy = ['currentUserId'];
		for(var k in cardFragmentDependenciesToCopy) {
			$scope.cardFragmentDependencies[cardFragmentDependenciesToCopy[k]] = $scope[cardFragmentDependenciesToCopy[k]];
		}

	});

})();
