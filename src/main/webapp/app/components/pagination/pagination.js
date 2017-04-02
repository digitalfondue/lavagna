(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgPagination', {
        templateUrl: 'app/components/pagination/pagination.html',
        bindings: {
            totalPages: '<',
            currentPage: '<',
            maxSize: '<',
            changePage: '&' // $page parameter
        },
        controller: function () {
            var ctrl = this;

            ctrl.$onChanges = function () {
                ctrl.pages = [];

                if (ctrl.totalPages <= ctrl.maxSize) {
                    for (var i = 1; i <= ctrl.totalPages;i++) {
                        ctrl.pages.push({value: i, text: i});
                    }
                } else {
                    var isFirstPage = ctrl.currentPage === 1;
                    var isLastPage = ctrl.currentPage === ctrl.totalPages;

                    if (!isFirstPage) {
                        ctrl.pages.push({value: 1, text: 1});
                    }

                    var middleElementsCount = ctrl.maxSize - (3 - (isFirstPage ? 1 : 0) - (isLastPage ? 1 : 0));
                    var elems = [ctrl.currentPage];
                    var cnt = 0;

                    // TODO: could be simplified by directly calculating the first and last elements...
                    while (elems.length <= middleElementsCount) {
                        var firstElems = cnt % 2 === 1;
                        var val;

                        cnt++;

                        if (firstElems) {
                            val = elems[0];

                            if (val > 2) {
                                elems.unshift(val - 1);
                            }
                        } else {
                            val = elems[elems.length - 1];

                            if (val + 1 < ctrl.totalPages) {
                                elems.push(val + 1);
                            }
                        }
                    }

                    if (elems[0] > 2) {
                        ctrl.pages.push({text: '...'});
                    }

                    angular.forEach(elems, function (v) {
                        ctrl.pages.push({value: v, text: v});
                    });

                    if (elems[elems.length - 1] < ctrl.totalPages - 1) {
                        ctrl.pages.push({text: '...'});
                    }

                    if (!isLastPage) {
                        ctrl.pages.push({value: ctrl.totalPages, text: ctrl.totalPages});
                    }
                }
            };
        }
    });
}());
