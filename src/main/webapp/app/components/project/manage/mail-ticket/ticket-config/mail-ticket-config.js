(function () {
    'use strict';

    angular.module('lavagna.components').component('lvgProjectManageMailTicketConfig', {
        bindings: {
            mailConfig: '<',
            ticketConfig: '<',
            onToggle: '&',
            onEdit: '&',
            onDelete: '&'
        },
        controller: ['$mdDialog', 'BoardCache', ProjectManageMailTicketConfigController],
        templateUrl: 'app/components/project/manage/mail-ticket/ticket-config/mail-ticket-config.html'
    });

    function ProjectManageMailTicketConfigController($mdDialog, BoardCache) {
        var ctrl = this;

        ctrl.$onInit = function onInit() {
            BoardCache.column(ctrl.ticketConfig.columnId).then(function (column) {
                ctrl.column = column;
            });
        };
    }
}());
