(function() {
    'use strict';

    angular.module('lavagna.components').component('lvgProjectManageMailTicketConfig', {
        bindings: {
            mailConfig: '<',
            ticketConfig: '<',
            onEdit: '&',
            onDelete: '&'
        },
        controller: ['$mdDialog', 'BoardCache', 'Project', ProjectManageMailTicketConfigController],
        templateUrl: 'app/components/project/manage/mail-ticket/ticket-config/mail-ticket-config.html'
    });

    function ProjectManageMailTicketConfigController($mdDialog, Project, BoardCache) {
        var ctrl = this;

        ctrl.$onInit = function onInit() {
            BoardCache.column(ticketConfig.columnId).then(function(column) {
                ctrl.column = column;
            });
        };
    }
})();
