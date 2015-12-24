(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgComponentAdminParameters', {
            bindings: {},
            controller: AdminParametersController,
            controllerAs: 'adminParamsCtrl',
            templateUrl: 'app/components/admin/parameters/parameters.html'
    });

    function AdminParametersController(Admin, Notification) {
        var ctrl = this;

        var loadAll = function() {
            ctrl.configurable = {};
            var configurableKeys = ['TRELLO_API_KEY', 'MAX_UPLOAD_FILE_SIZE', 'USE_HTTPS', 'EMAIL_NOTIFICATION_TIMESPAN'];
            angular.forEach(configurableKeys, function(v) {
                Admin.findByKey(v).then(function(res) {
                    ctrl.configurable[res.first] = res.second;
                });
            });

            Admin.findAllConfiguration().then(function (res) {
                ctrl.conf = res;
            });
        };

        loadAll();

        ctrl.updateConfiguration = function(k,v) {
            Admin.updateKeyConfiguration(k,v).then(function() {
                Notification.addAutoAckNotification('success', {
                    key: 'notification.admin-parameters.updateConfiguration.success',
                    parameters: { parameterName : k }
                }, false);
            }, function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-parameters.updateConfiguration.error',
                    parameters: { parameterName : k }
                }, false);
            }).then(loadAll)
        };

        ctrl.deleteConfiguration = function(k) {
            Admin.deleteKeyConfiguration(k).then(function() {
                Notification.addAutoAckNotification('success', {
                    key: 'notification.admin-parameters.deleteConfiguration.success',
                    parameters: { parameterName : k }
                }, false);
            }, function() {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-parameters.deleteConfiguration.error',
                    parameters: { parameterName : k }
                }, false);
            }).then(loadAll)
        };
    };
})();
