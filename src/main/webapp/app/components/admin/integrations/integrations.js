(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminIntegrations', {
        templateUrl: 'app/components/admin/integrations/integrations.html',
        controller: ['$mdDialog', '$translate', 'Notification', 'Integrations', 'Project', AdminIntegrationsController]
    });

    function AdminIntegrationsController($mdDialog, $translate, Notification, Integrations, Project) {
        var ctrl = this;
        var integrationCtrl = this;

        ctrl.addNewIntegrationDialog = addNewIntegrationDialog;
        ctrl.deleteDialog = deleteDialog;
        ctrl.editDialog = editDialog;
        ctrl.enable = enable;

        ctrl.$onInit = function () {
            loadAll();
        };

        function loadAll() {
            Integrations.getAll().then(function (res) { ctrl.integrations = res; });
            Project.list().then(function (res) { ctrl.projects = res; });
        }

        function addNewIntegrationDialog(event) {
            $mdDialog.show({
                targetEvent: event,
                templateUrl: 'app/components/admin/integrations/add-new-integration-dialog.html',
                controller: function () {
                    var ctrl = this;

                    ctrl.addParameter = addParameter;
                    ctrl.removeParameter = removeParameter;
                    ctrl.create = create;
                    ctrl.cancel = cancel;

                    ctrl.customIntegration = {
                        name: '',
                        code: '',
                        configuration: {},
                        projects: [],
                        metadata: {
                            description: '',
                            parameters: []
                        }
                    };

                    function addParameter() {
                        ctrl.customIntegration.metadata.parameters.push({'type': 'input', 'label': undefined, 'key': undefined});
                    }

                    function removeParameter(parameter) {
                        ctrl.customIntegration.metadata.parameters.splice(ctrl.customIntegration.metadata.parameters.indexOf(parameter), 1);
                    }

                    function create() {
                        angular.forEach(ctrl.customIntegration.metadata.parameters, function (v) {
                            ctrl.customIntegration.configuration[v.key] = v.value;
                            delete v.value;
                        });
                        Integrations.create(ctrl.customIntegration).then(function (integration) {
                            loadAll();
                            $mdDialog.cancel();
                            integrationCtrl.editDialog(integration, null);
                        });
                    }

                    function cancel() {
                        $mdDialog.cancel();
                    }
                },
                controllerAs: 'addNewIntegrationCtrl',
                bindToController: true,
                fullscreen: true
            });
        }

        function deleteDialog(integration, event) {
            var translationKeys = {name: integration.name};
            var confirm = $mdDialog.confirm()
                .title($translate.instant('admin.integrations.delete.title'))
                .textContent($translate.instant('admin.integrations.delete.message', translationKeys))
                .targetEvent(event)
                .ok($translate.instant('button.yes'))
                .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                return Integrations.remove(integration.name);
            }).then(function () {
                Notification.addAutoAckNotification('success', {key: 'notification.admin-integrations.remove.success', parameters: translationKeys}, false);
                loadAll();
            }, function (error) {
                loadAll();
                if (error) {
                    Notification.addAutoAckNotification('error', {key: 'notification.admin-integrations.remove.error', parameters: translationKeys}, false);
                }
            });
        }

        function enable(integration, status) {
            Integrations.enable(integration.name, status).then(function () {
                loadAll();
            });
        }

        var mainCtrl = ctrl;

        function editDialog(integration, event) {
            var translationKeys = {name: integration.name};

            $mdDialog.show({
                targetEvent: event,
                templateUrl: 'app/components/admin/integrations/edit-integration-dialog.html',
                controller: function () {
                    var ctrl = this;
                    ctrl.newParameters = [];

                    ctrl.integration = integration;
                    ctrl.script = integration.script;
                    ctrl.configuration = angular.copy(integration.configuration);
                    ctrl.enableForAllProjects = integration.projects === null;
                    ctrl.projects = angular.copy(integration.projects) || [];
                    ctrl.allProjects = mainCtrl.projects;

                    ctrl.addNewParameter = addNewParameter;
                    ctrl.removeNewParameter = removeNewParameter;
                    ctrl.removeParameter = removeParameter;
                    ctrl.cancel = cancel;
                    ctrl.save = save;
                    ctrl.togge = toggle;
                    ctrl.exists = exists;

                    function cancel() {
                        $mdDialog.cancel(false);
                    }

                    function save() {
                        var metadata = {
                            description: integration.metadata.description,
                            parameters: angular.isArray(integration.metadata.parameters) ? integration.metadata.parameters : []
                        };

                        // merge new properties
                        angular.forEach(ctrl.newParameters, function (v) {
                            ctrl.configuration[v.key] = v.value;

                            metadata.parameters.push({
                                key: v.key,
                                label: v.label,
                                type: v.type
                            });
                        });

                        // remove old properties
                        var configToRemove = [];

                        for (var key in ctrl.configuration) {
                            if (ctrl.configuration.hasOwnProperty(key)) {
                                var toRemove = true;

                                angular.forEach(metadata.parameters, function (param) {
                                    toRemove = toRemove && param.key !== key;
                                });

                                if (toRemove) {
                                    configToRemove.push(key);
                                }
                            }
                        }

                        angular.forEach(configToRemove, function (key) {
                            delete ctrl.configuration[key];
                        });

                        Integrations.update(integration.name, ctrl.script, ctrl.configuration, ctrl.enableForAllProjects ? null : ctrl.projects, metadata).then(function () {
                            ctrl.cancel();
                            Notification.addAutoAckNotification('success', {key: 'notification.admin-integrations.update.success', parameters: translationKeys}, false);
                            loadAll();
                        }, function (error) {
                            if (error) {
                                Notification.addAutoAckNotification('error', {key: 'notification.admin-integrations.update.error', parameters: translationKeys}, false);
                            }
                        });
                    }

                    function toggle(project) {
                        var idx = ctrl.projects.indexOf(project.shortName);

                        if (idx > -1) {
                            ctrl.projects.splice(idx, 1);
                        } else {
                            ctrl.projects.push(project.shortName);
                        }
                    }

                    function exists(project) {
                        return ctrl.projects.indexOf(project.shortName) > -1;
                    }

                    function removeParameter(parameter) {
                        ctrl.integration.metadata.parameters.splice(ctrl.integration.metadata.parameters.indexOf(parameter), 1);
                    }

                    function addNewParameter() {
                        ctrl.newParameters.push({'type': 'input', 'label': undefined, 'key': undefined});
                    }

                    function removeNewParameter(parameter) {
                        ctrl.newParameters.splice(ctrl.newParameters.indexOf(parameter), 1);
                    }
                },
                fullscreen: true,
                autoWrap: false,
                controllerAs: 'editIntegrationCtrl',
                bindToController: true
            });
        }
    }
}());
