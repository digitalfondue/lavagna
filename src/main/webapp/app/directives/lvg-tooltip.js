(function () {

    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgTooltip', function () {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                attrs.$observe('lvgTooltipHtml', function (html) {
                    if (html === undefined || html === null || html.length === 0) {
                        return;
                    }
                    $(element).tooltip({
                        items: '[data-lvg-tooltip], [lvg-tooltip]',
                        content: html,
                        show: {delay: 500},
                        close: function (event, ui) {
                            ui.tooltip.hover(
                                function () {
                                    $(this).stop(true).fadeTo(400, 1);
                                },
                                function () {
                                    $(this).fadeOut("400", function () {
                                        $(this).remove();
                                    })
                                }
                            );
                        }
                    });
                });
            }
        }
    });
})();
