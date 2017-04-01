(function () {
    'use strict';

    var components = angular.module('lavagna.components');

    // used for transient drag and drop data
    components.service('SharedBoardDataService', function (EventBus) {
        return {
            startDrag: function startDrag() {
                EventBus.emit('SharedBoardDataService.startDrag');
            },
            endDrag: function endDrag() {
                EventBus.emit('SharedBoardDataService.endDrag');
            },
            listenToDragStart: function listenToStartDrag(callback) {
                return EventBus.on('SharedBoardDataService.startDrag', callback);
            },
            listenToDragEnd: function listenToDragEnd(callback) {
                return EventBus.on('SharedBoardDataService.endDrag', callback);
            }
        };
    });
}());
