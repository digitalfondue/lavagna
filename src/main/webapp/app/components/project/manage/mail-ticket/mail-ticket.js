(function () {
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

        ctrl.showAddMailConfigDialog = function () {
            openMailConfigDialog().then(function (config) {
                return Project.createMailConfig(project.shortName,
                    config.name,
                    config.config,
                    config.subject,
                    config.body);
            }).then(loadConfigs);
        };

        ctrl.toggleMailConfig = function (mailConfig) {
            Project.updateMailConfig(project.shortName,
                mailConfig.id,
                mailConfig.name,
                !mailConfig.enabled,
                mailConfig.config,
                mailConfig.subject,
                mailConfig.body).then(loadConfigs);
        };

        ctrl.editMailConfig = function (mailConfig) {
            openMailConfigDialog(mailConfig).then(function (config) {
                return Project.updateMailConfig(project.shortName,
                                mailConfig.id,
                                config.name,
                                config.enabled,
                                config.config,
                                config.subject,
                                config.body);
            }).then(loadConfigs);
        };

        ctrl.deleteMailConfig = function (mailConfig) {
            Project.deleteMailConfig(project.shortName,
                mailConfig.id).then(loadConfigs);
        };

        ctrl.addTicketConfig = function (mailConfig) {
            openMailTicketConfigDialog().then(function (ticketConfig) {
                return Project.createMailTicket(project.shortName,
                    ticketConfig.name,
                    ticketConfig.alias,
                    ticketConfig.sendByAlias,
                    ticketConfig.overrideNotification,
                    ticketConfig.subject,
                    ticketConfig.body,
                    ticketConfig.columnId,
                    mailConfig.id,
                    '');
            }).then(loadConfigs);
        };

        ctrl.onToggleTicketConfig = function ($mailConfig, $ticketConfig) {
            Project.updateMailTicket(project.shortName,
                $ticketConfig.id,
                $ticketConfig.name,
                !$ticketConfig.enabled,
                $ticketConfig.alias,
                $ticketConfig.sendByAlias,
                $ticketConfig.overrideNotification,
                $ticketConfig.subject,
                $ticketConfig.body,
                $ticketConfig.columnId,
                $mailConfig.id,
                $ticketConfig.metadata).then(loadConfigs);
        };

        ctrl.onEditTicketConfig = function ($mailConfig, $ticketConfig) {
            openMailTicketConfigDialog($ticketConfig).then(function (config) {
                return Project.updateMailTicket(project.shortName,
                           $ticketConfig.id,
                           config.name,
                           $ticketConfig.enabled,
                           config.alias,
                           config.sendByAlias,
                           config.overrideNotification,
                           config.subject,
                           config.body,
                           config.columnId,
                           $mailConfig.id,
                           config.metadata);
            }).then(loadConfigs);
        };

        ctrl.onDeleteTicketConfig = function ($ticketConfig) {
            Project.deleteMailTicket(project.shortName,
                $ticketConfig.id).then(loadConfigs);
        };

        function openMailConfigDialog(mailConfig) {
            return $mdDialog.show({
                templateUrl: 'app/components/project/manage/mail-ticket/mail-config-dialog.html',
                bindToController: true,
                locals: {
                    configToEdit: mailConfig
                },
                controller: function () {
                    var ctrl = this;

                    function initConfig() {
                        ctrl.configToAdd = {
                            config: {}
                        };

                        if (ctrl.configToEdit) {
                            ctrl.configToAdd.name = ctrl.configToEdit.name;
                            ctrl.configToAdd.config = ctrl.configToEdit.config;
                            ctrl.configToAdd.subject = ctrl.configToEdit.subject;
                            ctrl.configToAdd.body = ctrl.configToEdit.body;
                        }
                    }

                    ctrl.addConfig = function () {
                        $mdDialog.hide(ctrl.configToAdd);
                    };

                    ctrl.cancel = function () {
                        $mdDialog.cancel();
                    };

                    initConfig();
                },
                controllerAs: '$mailConfigDialogCtrl'
            });
        }

        function openMailTicketConfigDialog(ticketConfig) {
            return $mdDialog.show({
                templateUrl: 'app/components/project/manage/mail-ticket/mail-ticket-config-dialog.html',
                controller: function () {
                    var ctrl = this;

                    function init() {
                        var config = {
                            name: null,
                            alias: null,
                            sendByAlias: null,
                            columnId: null,
                            boardShortName: null
                        };

                        Project.findBoardsInProject(project.shortName).then(function (boards) {
                            ctrl.boards = boards;

                            if (ticketConfig) {
                                config.name = ticketConfig.name;
                                config.alias = ticketConfig.alias;
                                config.sendByAlias = ticketConfig.sendByAlias;
                                config.overrideNotification = ticketConfig.overrideNotification;
                                config.subject = ticketConfig.subject;
                                config.body = ticketConfig.body;
                                config.columnId = ticketConfig.columnId;

                                return BoardCache.column(ticketConfig.columnId);
                            } else {
                                return {overrideNotification: false, boardShortName: null};
                            }
                        }).then(function (column) {
                            config.boardShortName = column.boardShortName;

                            if (column.boardShortName !== null) {
                                return loadBoardColumns(column.boardShortName);
                            }
                        }).then(function (columns) {
                            ctrl.columns = columns;
                        }).finally(function () {
                            ctrl.configToAdd = config;
                        });
                    }

                    function loadBoardColumns(shortName) {
                        return Board.columns(shortName).then(function (columns) {
                            var boardColumns = columns.filter(function (value) {
                                return value.location === 'BOARD';
                            });

                            return boardColumns;
                        });
                    }

                    ctrl.onChangeBoard = function (shortName) {
                        loadBoardColumns(shortName).then(function (columns) {
                            ctrl.configToAdd.columnId = null;
                            ctrl.columns = columns;
                        });
                    };

                    ctrl.add = function () {
                        $mdDialog.hide(ctrl.configToAdd);
                    };

                    ctrl.cancel = function () {
                        $mdDialog.cancel();
                    };

                    init();
                },
                controllerAs: '$mailTicketConfigDialogCtrl'
            });
        }

        function loadConfigs() {
            Project.getMailConfigs(project.shortName).then(function (configs) {
                ctrl.configs = configs;
            });
        }
    }
}());
