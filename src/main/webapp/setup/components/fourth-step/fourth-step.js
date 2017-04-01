(function () {
    var module = angular.module('lavagna-setup');

    module.component('setupFourthStep', {
        controller: ['$window', '$http', '$state', 'Configuration', SetupActivationCtrl],
        templateUrl: 'components/fourth-step/fourth-step.html'
    });

    function SetupActivationCtrl($window, $http, $state, Configuration) {
        var ctrl = this;

        ctrl.toSave = Configuration.toSave;
        ctrl.selectedAuthMethod = Configuration.selectedAuthMethod;
        ctrl.selectedNewOauthConf = Configuration.selectedNewOauthConf;

        ctrl.activate = function () {
            var configToUpdate = [{
                first: 'SETUP_COMPLETE',
                second: 'true'
            }].concat(ctrl.toSave.first, ctrl.toSave.second);

            $http.post('api/setup/', {toUpdateOrCreate: configToUpdate, user: ctrl.toSave.user}).then(goToRootApp);
        };

        ctrl.back = function () {
            $state.go('third-step');
        };

        function goToRootApp() {
            window.location.href = getOrigin(window) + window.location.pathname.replace(/setup\/$/, '');
        }

        function getOrigin(window) {
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + '//' + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
            }

            return window.location.origin;
        }
    }
}());
