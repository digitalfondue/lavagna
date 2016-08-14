(function () {

	'use strict';

	var services = angular.module('lavagna.services');
	
	var COMMENT_ICON = angular.element('<svg fill="#000000" height="100%" viewBox="0 0 24 24" width="100%" fit="" preserveAspectRatio="xMidYMid meet" focusable="false" xmlns="http://www.w3.org/2000/svg"><path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18zM18 14H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/><path d="M0 0h24v24H0z" fill="none"/></svg>')[0];
	var LIST_ICON = angular.element('<svg fill="#000000" height="100%" viewBox="0 0 24 24" width="100%" fit="" preserveAspectRatio="xMidYMid meet" focusable="false" xmlns="http://www.w3.org/2000/svg"><path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/><path d="M0 0h24v24H0z" fill="none"/></svg>')[0];
	var FILE_ICON = angular.element('<svg fill="#000000" height="100%" viewBox="0 0 24 24" width="100%" fit="" preserveAspectRatio="xMidYMid meet" focusable="false" xmlns="http://www.w3.org/2000/svg"><path d="M6 2c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13z"/><path d="M0 0h24v24H0z" fill="none"/></svg>')[0];
	var CLOCK_ICON = angular.element('<svg fill="#000000" height="100%" viewBox="0 0 24 24" width="100%" fit="" preserveAspectRatio="xMidYMid meet" focusable="false" xmlns="http://www.w3.org/2000/svg"><path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/><path d="M0 0h24v24H0z" fill="none"/><path d="M12.5 7H11v6l5.25 3.15.75-1.23-4.5-2.67z"/></svg>')[0];
	var MILESTONE_ICON = angular.element('<svg fill="#000000" height="100%" viewBox="0 0 24 24" width="100%" fit="" preserveAspectRatio="xMidYMid meet" focusable="false" xmlns="http://www.w3.org/2000/svg"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/><path d="M0 0h24v24H0z" fill="none"/></svg>')[0];
	
	
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
