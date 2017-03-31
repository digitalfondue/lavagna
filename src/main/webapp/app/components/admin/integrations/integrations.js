(function() {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgAdminIntegrations', {
        templateUrl: 'app/components/admin/integrations/integrations.html',
        controller: ['$mdDialog', '$translate', 'Notification', 'Integrations', 'Project', AdminIntegrationsController]
    });


    function AdminIntegrationsController($mdDialog, $translate, Notification, Integrations, Project) {

        var ctrl = this;

        ctrl.addNewIntegrationDialog = addNewIntegrationDialog;
        ctrl.deleteDialog = deleteDialog;
        ctrl.editDialog = editDialog;
        ctrl.enable = enable;

        ctrl.$onInit = function() {
            loadAll();
        };

        function loadAll() {
            Integrations.getAll().then(function(res) {ctrl.integrations = res;});
            Project.list().then(function(res) {ctrl.projects = res;});
        }

        function addNewIntegrationDialog(event) {
            $mdDialog.show({
                targetEvent: event,
                templateUrl: 'app/components/admin/integrations/add-new-integration-dialog.html',
                controller: function() {
                },
                controllerAs: 'addNewIntegrationCtrl',
                bindToController: true
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

            $mdDialog.show(confirm).then(function() {
                return Integrations.remove(integration.name);
            }).then(function() {
                Notification.addAutoAckNotification('success', {key: 'notification.admin-integrations.remove.success', parameters: translationKeys}, false);
                loadAll();
            }, function(error) {
                loadAll();
                if(error) {
                    Notification.addAutoAckNotification('error', {key: 'notification.admin-integrations.remove.error', parameters: translationKeys}, false);
                }
            })
        }

        function enable(integration, status) {
            Integrations.enable(integration.name, status).then(function() {
                loadAll();
            })
        }

        var mainCtrl = ctrl;

        function editDialog(integration, event) {
            var translationKeys = {name: integration.name};
            $mdDialog.show({
                targetEvent: event,
                templateUrl: 'app/components/admin/integrations/edit-integration-dialog.html',
                controller: function() {
                    var ctrl = this;
                    ctrl.integration = integration;
                    ctrl.configuration = angular.copy(integration.configuration);
                    ctrl.script = integration.script;

                    ctrl.enableForAllProjects = integration.projects === null;
                    ctrl.projects = angular.copy(integration.projects) || [];
                    ctrl.allProjects = mainCtrl.projects;

                    ctrl.cancel = function() {
                        $mdDialog.cancel(false);
                    }

                    ctrl.save = function() {
                        Integrations.update(integration.name, ctrl.script, ctrl.configuration, ctrl.enableForAllProjects ? null : ctrl.projects).then(function() {
                            ctrl.cancel();
                            Notification.addAutoAckNotification('success', {key: 'notification.admin-integrations.update.success', parameters: translationKeys}, false);
                            loadAll();
                        }, function(error) {
                            if(error) {
                                Notification.addAutoAckNotification('error', {key: 'notification.admin-integrations.update.error', parameters: translationKeys}, false);
                            }
                        });
                    }

                    ctrl.toggle = function(project) {
                        var idx = ctrl.projects.indexOf(project.shortName);
                        if (idx > -1) {
                            ctrl.projects.splice(idx, 1);
                        } else {
                            ctrl.projects.push(project.shortName);
                        }
                    }

                    ctrl.exists = function(project) {
                        return ctrl.projects.indexOf(project.shortName) > -1;
                    }
                },
                fullscreen: true,
                autoWrap: false,
                controllerAs: 'editIntegrationCtrl',
                bindToController: true
            });
        }
    }

})()
