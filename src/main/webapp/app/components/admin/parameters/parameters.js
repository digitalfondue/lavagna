(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminParameters', {
        templateUrl: 'app/components/admin/parameters/parameters.html',
        controller: ['Admin', 'Notification', '$q', AdminParametersController],
    });

    function AdminParametersController(Admin, Notification, $q) {
        var ctrl = this;

        var configurableKeys = ['TRELLO_API_KEY', 'MAX_UPLOAD_FILE_SIZE', 'EMAIL_NOTIFICATION_TIMESPAN'];

        ctrl.updateAllChangedConfiguration = updateAllChangedConfiguration;

        ctrl.$onInit = function init() {
            loadAll();
        };

        function loadAll() {
            ctrl.configurable = {};
            angular.forEach(configurableKeys, function (v) {
                Admin.findByKey(v).then(function (res) {
                    ctrl.configurable[res.first] = res.second;
                });
            });

            Admin.findAllConfiguration().then(function (res) {
                ctrl.conf = res;
            });
        }

        function updateAllChangedConfiguration() {
            Admin.findAllConfiguration().then(function (res) {
                // fetch all changed keys
                var changedKey = [];

                angular.forEach(configurableKeys, function (k) {
                    if (ctrl.configurable[k] !== res[k]) {
                        changedKey.push(k);
                    }
                });

                var updated = [];

                // update or delete
                angular.forEach(changedKey, function (k) {
                    var promise;

                    if (ctrl.configurable[k] === '' || ctrl.configurable[k] === null || ctrl.configurable[k] === undefined) {
                        promise = deleteConfiguration(k);
                    } else {
                        promise = updateConfiguration(k, ctrl.configurable[k]);
                    }
                    updated.push(promise);
                });

                // trigger refresh
                $q.all(updated).then(loadAll);
            });
        }

        function updateConfiguration(k, v) {
            return Admin.updateKeyConfiguration(k, v).then(function () {
                Notification.addAutoAckNotification('success', {
                    key: 'notification.admin-parameters.updateConfiguration.success',
                    parameters: { parameterName: k }
                }, false);
            }, function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-parameters.updateConfiguration.error',
                    parameters: { parameterName: k }
                }, false);
            });
        }

        function deleteConfiguration(k) {
            return Admin.deleteKeyConfiguration(k).then(function () {
                Notification.addAutoAckNotification('success', {
                    key: 'notification.admin-parameters.deleteConfiguration.success',
                    parameters: { parameterName: k }
                }, false);
            }, function () {
                Notification.addAutoAckNotification('error', {
                    key: 'notification.admin-parameters.deleteConfiguration.error',
                    parameters: { parameterName: k }
                }, false);
            });
        }
    }
}());
