(function () {
    'use strict';

    var module = angular.module('lavagna-setup', ['ui.router', 'ngSanitize', 'ngMessages', 'ngMaterial']);


    module.factory('lavagnaHttpInterceptor', ['$q', '$window', function ($q, $window) {
            //
        return {
            'request': function (config) {
                if (angular.isDefined($window.csrfToken) && $window.csrfToken !== null) {
                    config.headers['x-csrf-token'] = $window.csrfToken;
                }

                return config;
            },
            'response': function (response) {
                var headers = response.headers();

                if (headers['x-csrf-token']) {
                    $window.csrfToken = headers['x-csrf-token'];
                }

                return response;
            },
            'responseError': function (rejection) {
                // if the session has been lost, trigger a reload
                if (rejection.status === 401) {
                    $window.location.reload();
                }

                return $q.reject(rejection);
            }
        };
    }]);

    module.config(['$stateProvider', '$urlRouterProvider', '$httpProvider', function ($stateProvider, $urlRouterProvider, $httpProvider) {
        $stateProvider.state('first-step', {
            url: '/',
            template: '<setup-first-step></setup-first-step>',
        }).state('second-step', {
            url: '/login',
            template: '<setup-second-step></setup-second-step>',
        }).state('third-step', {
            url: '/user',
            template: '<setup-third-step></setup-third-step>',
        }).state('fourth-step', {
            url: '/confirmation',
            template: '<setup-fourth-step></setup-fourth-step>'
        });

        $urlRouterProvider.otherwise('/');

        $httpProvider.interceptors.push('lavagnaHttpInterceptor');
    }]);

    module.service('Configuration', function () {
        var conf = {
            toSave: {first: undefined, second: undefined, user: undefined},
            secondStep: {
                ldap: {},
                oauth: {},
                oauthProviders: ['bitbucket', 'gitlab', 'github', 'google', 'twitter'],
                oauthCustomizable: ['gitlab'],
                oauthNewProvider: {}
            }
        };

        angular.forEach(conf.secondStep.oauthProviders, function (p) {
            conf.secondStep.oauth[p] = {present: false};
        });

        return conf;
    });

    module.run(['Configuration', '$state', function (Configuration, $state) {
        // redirect to first page if the user is bypassing it (as we are using the rootscope for propagating the values)
        if (!Configuration.fromFirstStep) {
            $state.go('first-step');
        }
    }]);

    module.filter('mask', function () {
        return function (text) {
            if (text === undefined) {
                return null;
            }

            return text.replace(/./gi, '*');
        };
    });

    module.directive('lvgMatch', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                modelValueToMatch: '=lvgMatch'
            },
            link: function ($scope, $element, $attrs, ngModel) {
                ngModel.$validators.lvgMatch = function (ngModelValue) {
                    return ngModelValue === $scope.modelValueToMatch;
                };

                $scope.$watch($scope.modelValueToMatch, function () {
                    ngModel.$validate();
                });
            }
        };
    });
}());
