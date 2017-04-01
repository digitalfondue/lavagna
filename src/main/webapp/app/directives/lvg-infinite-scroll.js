(function () {
    'use strict';

    angular.module('lavagna.directives').directive('lvgInfiniteScroll', function ($document) {
        return {
            restrict: 'A',
            scope: {
                listen: '@',
                call: '&'
            },
            link: function (scope, $element) {
                var element = scope.listen ? $document[0].querySelector(scope.listen) : $element[0];
                var boundElement = $element[0];
                var loading = false;

                function shouldLoadMoreOnBoundElement() {
                    var elementRect = element.getBoundingClientRect();
                    var boundElementRect = boundElement.getBoundingClientRect();

                    return boundElementRect.bottom < elementRect.bottom;
                }

                function shouldLoadMoreOnElement() {
                    return element.scrollTop >= element.scrollHeight - element.offsetHeight - parseInt(element.offsetHeight / 2, 10);
                }

                function shouldLoadMore() {
                    return angular.equals(boundElement, element) ? shouldLoadMoreOnElement() : shouldLoadMoreOnBoundElement();
                }

                function scrollHandler() {
                    var loadMore = shouldLoadMore();

                    if (!loading && loadMore) {
                        loading = true;
                        scope.$applyAsync(scope.call);
                    }

                    loading = loading && loadMore;
                }

                angular.element(element).bind('scroll', scrollHandler);

                scope.$on('$destroy', function () {
                    angular.element(element).unbind('scroll', scrollHandler);
                });
            }
        };
    });
}());
