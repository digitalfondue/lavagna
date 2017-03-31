(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    services.factory('Title', function ($translate, $document, $timeout) {
        // based on stackoverflow.com/questions/23813599/set-page-title-using-ui-router
        // yep, it's a hack.
        return {
            set: function (_title, options) {
                $timeout(function () {
                    angular.element($document)[0].title = $translate.instant(_title, options);
                });
            }
        };
    });
}());
