(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    // simple wrapper
    services.factory('EventBus', ['$rootScope', EventBus]);

    function EventBus($rootScope) {
        return {
            on: function (eventName, callback) {
                return $rootScope.$on(eventName, callback);
            },

            emit: function (eventName, payload) {
                return $rootScope.$emit(eventName, payload);
            }
        };
    }
}());
