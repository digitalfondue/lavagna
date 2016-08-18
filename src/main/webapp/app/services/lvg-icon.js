(function () {

	'use strict';

	var services = angular.module('lavagna.services');
	
	var COMMENT_ICON = angular.element('<img src="/svg/ic_comment_gray_48px.svg" class="lvg-card-fragment-v2__icon">')[0];
	var LIST_ICON = angular.element('<img src="/svg/ic_list_gray_48px.svg" class="lvg-card-fragment-v2__icon">')[0];
	var FILE_ICON = angular.element('<img src="/svg/ic_insert_drive_file_gray_48px.svg" class="lvg-card-fragment-v2__icon">')[0];
	var CLOCK_ICON = angular.element('<img src="/svg/ic_access_time_gray_48px.svg" class="lvg-card-fragment-v2__icon">')[0];
	var MILESTONE_ICON = angular.element('<img src="/svg/ic_location_on_gray_48px.svg" class="lvg-card-fragment-v2__icon">')[0];
	
	
	services.factory('LvgIcon', function ($window) {
		return {
			comment: function() {
				return COMMENT_ICON.cloneNode(true);
			},
			list: function() {
				return LIST_ICON.cloneNode(true);
			},
			file: function() {
				return FILE_ICON.cloneNode(true);
			},
			clock: function() {
				return CLOCK_ICON.cloneNode(true);
			},
			milestone: function() {
				return MILESTONE_ICON.cloneNode(true);
			}
		};
	});
})();
