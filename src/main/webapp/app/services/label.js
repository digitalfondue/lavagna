(function () {
    'use strict';

    var services = angular.module('lavagna.services');

    var extractData = function (data) {
        return data.data;
    };

    services.factory('Label', function ($http, $filter) {
        return {
            findByProjectShortName: function (shortName) {
                return $http.get('api/project/' + shortName + '/labels').then(extractData);
            },

            useCount: function (labelId) {
                return $http.get('api/label/' + labelId + '/use-count').then(extractData);
            },

            /* label format is {name : string, color : int}*/
            add: function (projectName, label) {
                return $http.post('api/project/' + projectName + '/labels', label).then(extractData);
            },

            remove: function (labelId) {
                return $http['delete']('api/label/' + labelId).then(extractData);
            },

            /* label format is {name : string, color : int}*/
            update: function (labelId, label) {
                return $http.post('api/label/' + labelId, label).then(extractData);
            },

            /* label format is {name : string, color : int}*/
            updateSystemLabel: function (labelId, label) {
                return $http.post('api/label/' + labelId + '/system', label).then(extractData);
            },

            findLabelListValues: function (labelId) {
                return $http.get('api/label/' + labelId + '/label-list-values').then(extractData).then(function (res) {
                    angular.forEach(res, function (labelListValue) {
                        var toAdd = {};

                        angular.forEach(labelListValue.metadata, function (value, key) {
                            toAdd[key + 'Translated'] = $filter('translate')('label-list-value.metadata.' + key + '.' + value);
                        });
                        labelListValue.metadata = angular.extend({}, labelListValue.metadata, toAdd);
                    });

                    return res;
                });
            },

            countLabelListValueUse: function (labelListValueId) {
                return $http.get('api/label-list-values/' + labelListValueId + '/count-use').then(extractData).then(function (val) {
                    return parseInt(val, 10);
                });
            },

            addLabelListValue: function (labelId, listValue) {
                return $http.post('api/label/' + labelId + '/label-list-values', listValue).then(extractData);
            },

            removeLabelListValue: function (labelListValueId) {
                return $http['delete']('api/label-list-values/' + labelListValueId).then(extractData);
            },

            updateLabelListValue: function (lvl) {
                return $http.post('api/label-list-values/' + lvl.id, lvl).then(extractData);
            },

            moveLabelListValue: function (labelId, moveListValue) {
                return $http.post('api/label/' + labelId + '/label-list-values/move', moveListValue).then(extractData);
            },

            swapLabelListValues: function (labelId, swapListValue) {
                return $http.post('api/label/' + labelId + '/label-list-values/swap', swapListValue).then(extractData);
            },
            //

            updateLabelListValueMetadata: function (labelListValueId, key, value) {
                return $http.post('api/label-list-values/' + labelListValueId + '/metadata/' + key, {value: value}).then(extractData);
            },

            removeLabelListValueMetadata: function (labelListValueId, key) {
                return $http['delete']('api/label-list-values/' + labelListValueId + '/metadata/' + key).then(extractData);
            },
            //

            extractValue: function (label, value) {
                if (label.type === 'STRING') {
                    return this.stringVal(value.trim());
                } else if (label.type === 'INT') {
                    return this.intVal(value);
                } else if (label.type === 'TIMESTAMP') {
                    return this.dateVal($filter('extractISO8601Date')(value));
                } else if (label.type === 'CARD') {
                    return this.cardVal(value.id);
                } else if (label.type === 'USER') {
                    return this.userVal(value.id);
                } else if (label.type === 'LIST') {
                    return this.listVal(value.id);
                }

                return this.nullVal();
            },

            nullVal: function () {
                return {type: 'NULL', valueString: null, valueTimestamp: null, valueInt: null, valueCard: null, valueUser: null, valueList: null};
            },

            stringVal: function (val) {
                return {type: 'STRING', valueString: val, valueTimestamp: null, valueInt: null, valueCard: null, valueUser: null, valueList: null};
            },

            dateVal: function (val) {
                return {type: 'TIMESTAMP', valueString: null, valueTimestamp: val, valueInt: null, valueCard: null, valueUser: null, valueList: null};
            },

            intVal: function (val) {
                return {type: 'INT', valueString: null, valueTimestamp: null, valueInt: val, valueCard: null, valueUser: null, valueList: null};
            },

            cardVal: function (val) {
                return {type: 'CARD', valueString: null, valueTimestamp: null, valueInt: null, valueCard: val, valueUser: null, valueList: null};
            },

            userVal: function (val) {
                return {type: 'USER', valueString: null, valueTimestamp: null, valueInt: null, valueCard: null, valueUser: val, valueList: null};
            },

            listVal: function (val) {
                return {type: 'LIST', valueString: null, valueTimestamp: null, valueInt: null, valueCard: null, valueUser: null, valueList: val};
            }
        };
    });
}());
