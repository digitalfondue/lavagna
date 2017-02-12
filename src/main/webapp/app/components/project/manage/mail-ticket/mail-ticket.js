(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgProjectManageMailTicket', {
        bindings: {
            project: '<'
        },
        controller: ['$mdDialog', 'Board', 'BoardCache', 'Project', ProjectManageMailTicketController],
        templateUrl: '/app/components/project/manage/mail-ticket/mail-ticket.html'
    });

    function ProjectManageMailTicketController($mdDialog, Board, BoardCache, Project) {
        var ctrl = this;
        var project = ctrl.project;

        ctrl.configs = [];

        ctrl.$onInit = function onInit() {
            loadConfigs();
        };

        ctrl.showAddMailConfigDialog = function() {
            openMailConfigDialog().then(function(config) {
                return Project.createMailConfig(project.shortName, config.name, config.config, config.properties);
            }).then(loadConfigs);
        };

        ctrl.addTicketConfig = function(mailConfig) {
            openMailTicketConfigDialog().then(function(ticketConfig) {
                return Project.createMailTicket(project.shortName,
                    ticketConfig.name,
                    ticketConfig.alias,
                    ticketConfig.sendByAlias,
                    ticketConfig.columnId,
                    mailConfig.id,
                    '');
            }).then(loadConfigs);
        };

        ctrl.toggleMailConfig = function(mailConfig) {
            Project.updateMailConfig(project.shortName,
                mailConfig.id,
                mailConfig.name,
                !mailConfig.enabled,
                mailConfig.config,
                mailConfig.properties).then(loadConfigs);
        };

        ctrl.editMailConfig = function(mailConfig) {
            openMailConfigDialog(mailConfig).then(function(config) {
                return Project.updateMailConfig(project.shortName,
                                mailConfig.id,
                                config.name,
                                config.enabled,
                                config.config,
                                config.properties);
            }).then(loadConfigs);
        };

        function openMailConfigDialog(mailConfig) {
            return $mdDialog.show({
                templateUrl: 'app/components/project/manage/mail-ticket/mail-config-dialog.html',
                bindToController: true,
                locals: {
                    configToEdit: mailConfig
                },
                controller: function() {
                    var ctrl = this;

                    function initConfig() {
                        ctrl.configToAdd = {
                            config: {},
                            properties: {}
                        };

                        if(ctrl.configToEdit) {
                            ctrl.configToAdd.name = ctrl.configToEdit.name;
                            ctrl.configToAdd.config = ctrl.configToEdit.config;
                            ctrl.configToAdd.properties = ctrl.configToEdit.properties;
                        }
                    }

                    ctrl.addConfig = function() {
                        $mdDialog.hide(ctrl.configToAdd);
                    };

                    ctrl.close = function() {
                        $mdDialog.cancel();
                    }

                    initConfig();
                },
                controllerAs: '$mailConfigDialogCtrl'
           });
        }

        function openMailTicketConfigDialog(mailConfig, ticketConfig) {
            return $mdDialog.show({
                templateUrl: 'app/components/project/manage/mail-ticket/mail-ticket-config-dialog.html',
                controller: function() {
                    var ctrl = this;

                    function init() {
                        var config = {
                            name: null,
                            alias: null,
                            sendByAlias: null,
                            columnId: null,
                            boardShortName: null
                        };

                        Project.findBoardsInProject(project.shortName).then(function(boards) {
                            ctrl.boards = boards;

                            if(ticketConfig) {
                                config.name = ticketConfig.name;
                                config.alias = ticketConfig.alias;
                                config.sendByAlias = ticketConfig.sendByAlias;
                                config.columnId = ticketConfig.columnId;

                                return BoardCache.column(ticketConfig.columnId);
                            } else {
                                return {boardShortName: null}
                            }
                        }).then(function(column) {
                            config.boardShortName = column.boardShortName;
                        }).finally(function() {
                            ctrl.configToAdd = config;
                        });
                    }

                    ctrl.onChangeBoard = function (shortName) {
                        Board.columns(shortName).then(function(columns) {
                            var boardColumns = columns.filter(function(value) {
                                return value.location === 'BOARD'
                            });

                            ctrl.configToAdd.columnId = null;
                            ctrl.columns = boardColumns;
                        });
                    }

                    ctrl.add = function() {
                        $mdDialog.hide(ctrl.configToAdd);
                    };

                    ctrl.cancel = function() {
                        $mdDialog.cancel();
                    }

                    init();
                },
                controllerAs: '$mailTicketConfigDialogCtrl'
            });
        }

        function loadConfigs() {
            Project.getMailConfigs(project.shortName).then(function(configs) {
                ctrl.configs = configs;
            });
        }
    }
})();
