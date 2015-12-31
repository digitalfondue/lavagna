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
		
		$scope.moveToPage = function(page) {
			$log.debug('move to page', page);
			var loc = $location.search();
			loc.page = page;
			$location.search(loc);
			triggerSearch();
		};
		
		$scope.selected = {};
		
		function selectedCardsCount() {
			var cnt = 0;
			for(var project in $scope.selected) {
				for(var cardId in $scope.selected[project]) {
					if($scope.selected[project][cardId]) {
						cnt++;
					}
				}
			}
			return cnt;
		}
		
		$scope.selectedCardsCount = selectedCardsCount;

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
							if(!$scope.selected[shortProjectName]) {
								$scope.selected[shortProjectName] = {};
							}
							$scope.selected[shortProjectName][idsToSetAsTrue[i]] = true;
						}
					}
				})(projects[proj], proj));
			}
		};

		$scope.deselectAllInPage = function() {
			for(var project in $scope.selected) {
				for(var i = 0;i<$scope.found.length;i++) {
					delete $scope.selected[project][$scope.found[i].id];
				}
			}
		};

		triggerSearch();

		$scope.$on('refreshSearch', function() {$scope.selected = {}; triggerSearch();});

		function collectIdsByProject() {
			var res = {};

			for(var projectShortName in $scope.selected) {
				for(var cardId in $scope.selected[projectShortName]) {
					if($scope.selected[projectShortName][cardId]) {
						if(!res[projectShortName]) {
							res[projectShortName] = [];
						}
						res[projectShortName].push(cardId);
					}
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
