(function () {

    'use strict';

    var directives = angular.module('lavagna.directives');

    directives.directive('lvgLabelListVal', function ($q, $stateParams, $state, $compile, StompClient, LabelCache) {

        var loadListValue = function (scope) {

            var labelId = scope.labelValue.labelId;
            var listValueId = scope.labelValue.value.valueList;

            var fetchLabel = LabelCache.findByProjectShortNameAndLabelId($stateParams.projectName, labelId);
            var fetchListValue = LabelCache.findLabelListValue(labelId, listValueId);

            $q.all([fetchLabel, fetchListValue]).then(function(data){
                var label = data[0];
                var listValue = data[1];

                if (label.domain === 'SYSTEM' && label.name === 'MILESTONE') {
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
