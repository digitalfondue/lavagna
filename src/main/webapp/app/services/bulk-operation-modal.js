(function () {
    var services = angular.module('lavagna.services');

    services.factory('BulkOperationModal', function ($mdDialog, Card, User, BulkOperations, Label, $translate) {
        function moveTo(toMove, location) {
            var title = $translate.instant('dialog-move-to.' + location);
            var confirm = $mdDialog.confirm()
                .title(title)
                .ariaLabel(title)
                .ok($translate.instant('button.yes'))
                .cancel($translate.instant('button.no'));

            $mdDialog.show(confirm).then(function () {
                angular.forEach(toMove, function (cardIds, columnId) {
                    Card.moveAllFromColumnToLocation(columnId, cardIds, location);
                });
            });
        }

        return {
            moveToArchive: function (toMove) {
                moveTo(toMove, 'ARCHIVE');
            },
            moveToBacklog: function (toMove) {
                moveTo(toMove, 'BACKLOG');
            },
            moveToTrash: function (toMove) {
                moveTo(toMove, 'TRASH');
            },

            assignTo: function (cards, applyIfPresent) {
                applyIfPresent = applyIfPresent || angular.noop;

                $mdDialog.show({
                    template: '<lvg-dialog-select-user dialog-title="vm.title" cards="vm.cards" action="vm.action($user)"></lvg-dialog-select-user>',
                    locals: {
                        title: 'dialog-select-user.assign',
                        action: function (user) {
                            BulkOperations.assign(cards, user).then(applyIfPresent);
                        }
                    },
                    bindToController: true,
                    controller: function () {},
                    controllerAs: 'vm'
                });
            },

            removeAssignTo: function (cards, applyIfPresent) {
                applyIfPresent = applyIfPresent || angular.noop;

                $mdDialog.show({
                    template: '<lvg-dialog-select-user dialog-title="vm.title" cards="vm.cards" action="vm.action($user)"></lvg-dialog-select-user>',
                    locals: {
                        title: 'dialog-select-user.remove',
                        action: function (user) {
                            BulkOperations.removeAssign(cards, user).then(applyIfPresent);
                        }
                    },
                    bindToController: true,
                    controller: function () {},
                    controllerAs: 'vm'
                });
            },

            reAssignTo: function (cards, applyIfPresent) {
                applyIfPresent = applyIfPresent || angular.noop;

                $mdDialog.show({
                    template: '<lvg-dialog-select-user dialog-title="vm.title" cards="vm.cards" action="vm.action($user)"></lvg-dialog-select-user>',
                    locals: {
                        title: 'dialog-select-user.reassign',
                        action: function (user) {
                            BulkOperations.reassign(cards, user).then(applyIfPresent);
                        }
                    },
                    bindToController: true,
                    controller: function () {},
                    controllerAs: 'vm'
                });
            },

            setDueDate: function (cards, applyIfPresent) {
                applyIfPresent = applyIfPresent || angular.noop;
                $mdDialog.show({
                    template: '<lvg-dialog-select-date dialog-title="vm.title" action="vm.action($date)"></lvg-dialog-select-date>',
                    locals: {
                        title: 'dialog-select-date.set',
                        action: function (dueDate) {
                            BulkOperations.setDueDate(cards, dueDate).then(applyIfPresent);
                        }
                    },
                    bindToController: true,
                    controller: function () {},
                    controllerAs: 'vm'
                });
            },

            removeDueDate: function removeDueDate(cards, applyIfPresent) {
                var title = $translate.instant('dialog-remove-due-date.title');
                var confirm = $mdDialog.confirm().title(title)
                  .ariaLabel(title)
                  .ok($translate.instant('button.yes'))
                  .cancel($translate.instant('button.no'));

                $mdDialog.show(confirm).then(function () {
                    applyIfPresent = applyIfPresent || angular.noop;
                    BulkOperations.removeDueDate(cards).then(applyIfPresent);
                }, function () {});
            },

            removeMilestone: function (cards, applyIfPresent) {
                var title = $translate.instant('dialog-remove-milestone.title');
                var confirm = $mdDialog.confirm().title(title)
                  .ariaLabel(title)
                  .ok($translate.instant('button.yes'))
                  .cancel($translate.instant('button.no'));

                $mdDialog.show(confirm).then(function () {
                    applyIfPresent = applyIfPresent || angular.noop;
                    BulkOperations.removeMilestone(cards).then(applyIfPresent);
                }, function () {});
            },

            addLabel: function (cards, projectName, applyIfPresent) {
                applyIfPresent = applyIfPresent || angular.noop;
                $mdDialog.show({
                    template: '<lvg-dialog-select-label dialog-title="title" action="action($label, $value)" project-name="projectName" button-label="button" with-label-value-picker="true"></lvg-dialog-select-label>',
                    controller: function ($scope) {
                        $scope.title = 'add';
                        $scope.button = 'button.add';
                        $scope.projectName = projectName;
                        $scope.action = function (labelToAdd, labelValueToAdd) {
                            BulkOperations.addLabel(cards, labelToAdd, Label.extractValue(labelToAdd, labelValueToAdd)).then(applyIfPresent);
                        };
                    }
                });
            },

            removeLabel: function (cards, projectName, applyIfPresent) {
                applyIfPresent = applyIfPresent || angular.noop;
                $mdDialog.show({
                    template: '<lvg-dialog-select-label dialog-title="title" action="action($label)" project-name="projectName" button-label="button" ></lvg-dialog-select-label>',
                    controller: function ($scope) {
                        $scope.title = 'remove';
                        $scope.button = 'button.remove';
                        $scope.projectName = projectName;
                        $scope.action = function (labelToRemove) {
                            BulkOperations.removeLabel(cards, labelToRemove).then(applyIfPresent);
                        };
                    }
                });
            }
        };
    });
}());
