(function () {

    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgLabelListVal', function ($stateParams, $state, $compile, StompClient, LabelCache) {

        var loadListValue = function (scope) {

            var labelId = scope.labelValue.labelId;
            var listValueId = scope.labelValue.labelValueList;
            var labelDomain = scope.labelValue.labelDomain;
            var labelName = scope.labelValue.labelName;

            return LabelCache.findLabelListValue(labelId, listValueId).then(function (listValue) {
                if (labelDomain === 'SYSTEM' && labelName === 'MILESTONE') {
                    scope.labelLink = $state.href('projectMilestone', {
                        projectName: $stateParams.projectName,
                        milestone: listValue.value
                    });
                    scope.displayValue = listValue.value;
                    scope.metadata = listValue.metadata;
                } else {
                    scope.displayValue = listValue.value;
                    scope.metadata = listValue.metadata;
                    scope.noLink = true;
                }
            });
        };

        var listValTemplate = '<a data-ng-if="::labelLink" href="{{::labelLink}}" ng-class="{\'strike\' : metadata.status === \'CLOSED\'}">{{::displayValue}}</a>' +
            '<span data-ng-if="::noLink" ng-class="{\'strike\' : metadata.status === \'CLOSED\'}" ng-bind="::displayValue"></span>';

        return {
            template: listValTemplate,
            scope: {
                labelValue: '='
            },
            restrict: 'E',
            link: function ($scope, $element, attrs) {

                loadListValue($scope);

                StompClient.subscribe($scope, '/event/label-list-values/' + $scope.labelValue.labelValueList, function () {
                    loadListValue($scope).then(function () {
                        $element.html($compile(listValTemplate)($scope));
                    });
                });

            }
        };
    });
})();
