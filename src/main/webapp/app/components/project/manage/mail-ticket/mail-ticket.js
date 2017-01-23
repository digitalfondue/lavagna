(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgProjectManageMailTicket', {
        bindings: {
            project: '<'
        },
        controller: ProjectManageMailTicketController,
        templateUrl: '/app/components/project/manage/mail-ticket/mail-ticket.html'
    });

    function ProjectManageMailTicketController($mdDialog, Project) {
        var ctrl = this;

        ctrl.configs = [];

        ctrl.$onInit = function onInit() {
            loadConfigs();
        };

        function loadConfigs() {
            Project.getMailConfigs(ctrl.project.shortName).then(function(configs) {
                ctrl.configs = configs;
            });
        }

        ctrl.showAddMailConfigDialog = function() {
            $mdDialog.show({
                templateUrl: 'app/components/project/manage/mail-ticket/add-mail-config-dialog.html',
                controller: function() {
                    var ctrl = this;

                    function initConfig() {
                        ctrl.configToAdd = {
                            config: {},
                            properties: {}
                        };
                    }

                    ctrl.addConfig = addMailConfig;

                    ctrl.close = function() {
                        $mdDialog.hide();
                    };

                    initConfig();
                },
                controllerAs: 'addMailConfigDialogCtrl'
            });
        };

        function addMailConfig(config) {
            Project.createMailConfig(ctrl.project.shortName, config.name, config.config, config.properties).then(loadConfigs);
        }
    }
})();
