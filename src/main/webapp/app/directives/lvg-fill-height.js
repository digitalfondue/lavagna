(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgUpdateSidebarSize', function ($window) {
        return {
            restrict: 'A',
            link: function lvgUpdateSidebarSizeLink($scope, element, attrs) {
                var domElement = element[0];

                var resizeHandler = function lvgFillSidebarCardsHeightResizeHandler(hasMore) {
                    var wHeight = $window.innerHeight;//
                    var maxHeight = wHeight - 72; // fixed header + margins

                    var head = domElement.querySelector('.lvg-board-sidebar__header');
                    var panelHeadHeight = head ? head.offsetHeight : 0;

                    var panelFooterHeight = hasMore ? 48 : 0;

                    domElement.querySelector('.lvg-board-sidebar__content').style.maxHeight = (maxHeight - panelHeadHeight - panelFooterHeight) + 'px';
                };

                resizeHandler();

                $window.addEventListener('resize', resizeHandler);

                $scope.$on('$destroy', function () {
                    $window.removeEventListener('resize', resizeHandler);
                });

                // sidebar related
                attrs.$observe('lvgUpdateSidebarSizeTrigger', resizeHandler);
            }
        };
    });
}());
