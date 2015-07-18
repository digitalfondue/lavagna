(function () {

	'use strict';

	var module = angular.module('lavagna.controllers');

	var groupByDate = function (events, $filter) {

		var groupedByDate = {};
		var keyOrder = [];

		for (var i in events) {
			var dateRepresentation = $filter('date')(events[i].time, 'dd.MM.yyyy');
			if (keyOrder.indexOf(dateRepresentation) == -1) {
				keyOrder.push(dateRepresentation);
				groupedByDate[dateRepresentation] = [];
			}

			groupedByDate[dateRepresentation].push(events[i]);
		}

		return {groupedByDate: groupedByDate, keyOrder: keyOrder};
	};

	var loadUser = function (profile, $scope, $filter) {
		$scope.profile = profile;
		$scope.user = profile.user;

		$scope.hasMore = profile.latestActivity.length > 20;

		$scope.profile.latestActivity20 = profile.latestActivity.slice(0, 20);
		$scope.profile.eventsGroupedByDate = groupByDate($scope.profile.latestActivity20, $filter);
		return profile;
	};

	var showCalHeatMap = function (profile, $scope) {


		var parser = function (data) {
			var stats = {};
			for (var d in data) {
				stats[data[d].date / 1000] = data[d].count;
			}
			return stats;
		};

		var currentDate = new Date();
		var lastYear = new Date(currentDate.getFullYear(), currentDate.getMonth() - 11, 1);


		if ($scope.cal) {
			$scope.cal.update(profile.dailyActivity, parser);
		} else {
			$scope.cal = new CalHeatMap();
			$scope.cal.init({
				data: profile.dailyActivity,
				afterLoadData: parser,
				domainDynamicDimension: true,
				start: lastYear,
				id: "graph_c",
				domain: "month",
				subDomain: "day",
				range: 12,
				cellPadding: 2,
				itemName: ["event", "events"]
			});
		}
	};

	var setUserInfo = function ($stateParams, $scope) {
		$scope.userProvider = $stateParams.provider;
		$scope.userUsername = $stateParams.username;
	}

	module.controller('UserCtrl', function ($stateParams, $scope, $filter, $location, User) {

		setUserInfo($stateParams, $scope);

		$scope.userNameProfile = $stateParams.username;

		User.isCurrentUser($stateParams.provider, $stateParams.username).then(function (res) {
			$scope.isCurrentUser = res;
		});

		User.getUserProfile($stateParams.provider, $stateParams.username, 0)
			.then(function (profile) {
				return loadUser(profile, $scope, $filter);
			})
			.then(function (profile) {
				showCalHeatMap(profile, $scope)
			});
	});


	module.controller('UserActivityCtrl', function ($stateParams, $scope, $filter, $location, User) {

		setUserInfo($stateParams, $scope);

		$scope.page = 0;

		$scope.userNameProfile = $stateParams.username;

		User.isCurrentUser($stateParams.provider, $stateParams.username).then(function (res) {
			$scope.isCurrentUser = res;
		});

		$scope.loadFor = function (page) {
			User.getUserProfile($stateParams.provider, $stateParams.username, page)
				.then(function (profile) {
					return loadUser(profile, $scope, $filter);
				})
				.then(function (profile) {
					showCalHeatMap(profile, $scope)
				}).then(function () {
					$scope.page = page
				});
		};

		$scope.loadFor(0);
	})

	module.controller('UserProjectsCtrl', function ($stateParams, $scope, $filter, $location, User) {

		setUserInfo($stateParams, $scope);

		$scope.userNameProfile = $stateParams.username;

		User.isCurrentUser($stateParams.provider, $stateParams.username).then(function (res) {
			$scope.isCurrentUser = res;
		});

		User.getUserProfile($stateParams.provider, $stateParams.username, 0).then(function (profile) {
			loadUser(profile, $scope, $filter);
		})
	})

})();
