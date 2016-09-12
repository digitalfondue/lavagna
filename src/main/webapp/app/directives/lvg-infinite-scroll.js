(function() {
    'use strict';

    angular.module('lavagna.directives').directive('lvgInfiniteScroll', function ($document) {
        return {
            restrict: 'A',
            scope: {
                listen: '@',
                call: '&'
            },
            link: function (scope, $element, $attrs) {
                var element = scope.listen ? $document[0].querySelector(scope.listen) : $element[0];
                var boundElement = $element[0];
                var loading = false;

                var shouldLoadMoreOnBoundElement = function () {
                    var elementRect = element.getBoundingClientRect();
                    var boundElementRect = boundElement.getBoundingClientRect();

                    return boundElementRect.bottom < elementRect.bottom;
                };

                var shouldLoadMoreOnElement = function () {
                    return element.scrollTop >= element.scrollHeight - element.offsetHeight - parseInt(element.offsetHeight / 2, 10);
                };

                var shouldLoadMore = function () {
                    return boundElement == element ? shouldLoadMoreOnElement() : shouldLoadMoreOnBoundElement();
                };

                var scrollHandler = function () {
                    var loadMore = shouldLoadMore();

                    if(!loading && loadMore) {
                        loading = true;
                        scope.$apply(scope.call);
                    }

                    loading = loading && loadMore;
                };

                angular.element(element).bind("scroll", scrollHandler);

                scope.$on('$destroy', function () {
                    angular.element(element).unbind("scroll", scrollHandler);
                });
            }
        }
    });
})();
