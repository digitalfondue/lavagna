(function() {
    'use strict';

    angular.module('lavagna.filters').filter('filterByDataId', function() {
        return function(activityList, objectMap) {
            if(objectMap === undefined || objectMap === null) {
                return activityList;
            };

            var processedObjectIds = [];
            var filteredList = [];

            angular.forEach(activityList, function(activity) {
                if(objectMap[activity.dataId] != undefined && processedObjectIds.indexOf(activity.dataId) < 0) {
                    filteredList.push(activity);
                    processedObjectIds.push(activity.dataId);
                }
            });

            return filteredList;
        }
    });
})();
