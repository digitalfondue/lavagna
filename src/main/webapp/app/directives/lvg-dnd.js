(function () {
    'use strict';

    var directives = angular.module('lavagna.directives');

    // imported from https://github.com/angular/angular.js/blob/master/src/ng/directive/ngRepeat.js
    // which is under the MIT License (https://github.com/angular/angular.js/blob/master/LICENSE)

    var ngRepeatRegex = /^\s*([\s\S]+?)\s+in\s+([\s\S]+?)(?:\s+as\s+([\s\S]+?))?(?:\s+track\s+by\s+([\s\S]+?))?\s*$/;
    //

    directives.directive('lvgDnd', function ($parse, $log, User) {
        return {
            restrict: 'A',
            link: function ($scope, $element, attrs) {
                var opts = angular.extend({}, $parse(attrs.lvgDndOpts)($scope));

                var parsedListExpression = $parse(attrs.lvgDnd);
                var parsedDndDragstart = $parse(attrs.lvgDndDragstart);
                var parsedDndDrop = $parse(attrs.lvgDndDrop);
                var parsedDndEnd = $parse(attrs.lvgDndDragend);
                var requiredPermission = attrs.lvgDndPermission;

                opts.onStart = function onStart(event) {
                    if (opts.draggingClass) {
                        $element.addClass(opts.draggingClass);
                    }

                    var modelList = getModelList();
                    var ngRepeatList = getNgRepeatList(event.item);

                    if (ngRepeatList) {
                        var $item = ngRepeatList[event.oldIndex];
                        var $index = modelList.indexOf($item);

                        $scope.$evalAsync(function () {
                            parsedDndDragstart($scope, {'$item': $item, '$index': $index});
                        });
                    } else {
                        $log.debug('no ng-repeat or incorrect expression in element', event.item);
                    }
                };

                opts.onAdd = function onAdd(event) {
                    onDrop(event.newIndex, event.oldIndex);
                };

                opts.onUpdate = function onUpdate(event) {
                    onDrop(event.newIndex, event.oldIndex);
                };

                opts.onEnd = function onEnd() {
                    if (opts.draggingClass) {
                        $element.removeClass(opts.draggingClass);
                    }

                    $scope.$evalAsync(function () {
                        parsedDndEnd($scope);
                    });
                };

                function onDrop(index, oldIndex) {
                    $scope.$evalAsync(function () {
                        parsedDndDrop($scope, {'$index': index, '$oldIndex': oldIndex});
                    });
                }

                function getModelList() {
                    return parsedListExpression($scope);
                }

                function extractNgRepeatAttribute(item) {
                    for (var i = 0; i < item.attributes.length;i++) {
                        if (attrs.$normalize(item.attributes[i].name) === 'ngRepeat') {
                            return item.attributes[i].value;
                        }
                    }

                    return false;
                }

                // the list used by ng-repeat (could be filtered)
                function getNgRepeatList(item) {
                    var ngRepeatExpression = extractNgRepeatAttribute(item);

                    if (!ngRepeatExpression) {
                        return false;
                    }

                    var match = ngRepeatExpression.match(ngRepeatRegex);

                    if (!match) {
                        return false;
                    }

                    var listAndFilterExpression = match[2];

                    return $parse(listAndFilterExpression)($scope);
                }

                var sortableInstance = {
                    destroy: angular.noop
                };

                if (angular.isDefined(requiredPermission)) {
                    User.hasPermission(requiredPermission, undefined, true).then(function (res) {
                        if (res) {
                            sortableInstance = Sortable.create($element[0], opts);
                        }
                    })
                } else {
                    sortableInstance = Sortable.create($element[0], opts);
                }

                $scope.$on('$destroy', function onDestroySortable() {
                    sortableInstance.destroy();
                });
            }
        };
    });
}());
