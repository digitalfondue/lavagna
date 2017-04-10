(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgSearchResult', {
        templateUrl: 'app/components/search-result/search-result.html',
        bindings: {
            count: '=',
            page: '=',
            query: '=',
            moveToPage: '<',
            found: '<',
            project: '=',
            selected: '=',
            user: '<',
            totalPages: '<',
            countPerPage: '<',
            currentPage: '<',
            requiredPermissions: '<'
        },
        controller: function (Project) {
            var ctrl = this;

            var projects = {};

            ctrl.metadatas = {};

            ctrl.$onChanges = function () {
                if (ctrl.found && ctrl.found.length) {
                    for (var i = 0; i < ctrl.found.length;i++) {
                        var card = ctrl.found[i];

                        if (projects[card.projectShortName] === undefined) {
                            projects[card.projectShortName] = Project.loadMetadataAndSubscribe(card.projectShortName, ctrl.metadatas, true);
                        }
                    }
                }
            };

            ctrl.$onDestroy = function onDestroy() {
                angular.forEach(projects, function (subscription) {
                    subscription();
                });
            };
        }
    });
}());
