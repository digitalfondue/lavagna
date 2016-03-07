(function () {

    'use strict';

    var components = angular.module('lavagna.components');

    components.component('lvgCardFragment', {
        templateUrl: 'app/components/card-fragment/card-fragment.html',
        bindings: {
            card: '=',
            project: '=',
            board: '=',
            selected: '=',
            query: '=',
            page: '=',
            dependencies: '=',
            boardColumns: '=',
            view: '@',
            searchType: '=',
            readOnly: '@'
        },
        controller: CardFragmentController,
        controllerAs: 'cardFragmentCtrl'
    });

    function CardFragmentController($scope, Card, User, LabelCache) {
        var ctrl = this;

        ctrl.readOnly = ctrl.readOnly != undefined;
        ctrl.hideAssignedUsers = ctrl.hideAssignedUsers != undefined;

        ctrl.listView = ctrl.view != undefined && ctrl.view == 'list';
        ctrl.boardView = ctrl.view != undefined && ctrl.view == 'board';
        ctrl.searchView = ctrl.view != undefined && ctrl.view == 'search';

        //dependencies
        angular.extend(ctrl, ctrl.dependencies);

        User.currentCachedUser().then(function (user) {
            ctrl.currentUserId = user.id;
        });

        ctrl.hasUserLabels = function (cardLabels) {
            if (cardLabels === undefined || cardLabels.length === 0) {
                return false; //empty, no labels at all
            }
            for (var i = 0; i < cardLabels.length; i++) {
                if (cardLabels[i].labelDomain === 'USER') {
                    return true;
                }
            }
            return false;
        };

        ctrl.hasSystemLabelByName = function (labelName, cardLabels) {
            if (cardLabels === undefined || cardLabels.length === 0)
                return false; //empty, no labels at all
            for (var i = 0; i < cardLabels.length; i++) {
                if (cardLabels[i].labelName == labelName && cardLabels[i].labelDomain === 'SYSTEM') {
                    return true;
                }
            }
            return false;
        };

        var getMilestoneDate = function (label) {
            LabelCache.findLabelListValue(label.labelId, label.labelValueList).then(function (value) {
                label.releaseDate = value.metadata.releaseDate ? moment.utc(value.metadata.releaseDate, 'DD.MM.YYYY').valueOf() : null;
            });
        };

        var getMilestoneDates = function (card) {
            for (var i = 0; card && card.labels && i < card.labels.length; i++) {
                var label = card.labels[i];
                if (label.labelName == 'MILESTONE' && label.labelDomain === 'SYSTEM') {
                    getMilestoneDate(label);
                }
            }
        };

        $scope.$watch('cardFragmentCtrl.card', function (val) {
            getMilestoneDates(val);
        });

        ctrl.isSelfWatching = function (cardLabels, userId) {
            return Card.isWatchedByUser(cardLabels, userId)
        };

        ctrl.isAssignedToCard = function (cardLabels, userId) {
            return Card.isAssignedToUser(cardLabels, userId);
        };
    }
})();
