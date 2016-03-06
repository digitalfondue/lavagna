(function () {

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCardModal', function ($mdDialog, $state) {
		return {
			restrict: 'E',
            scope: {
                project: '=',
                board: '=',
                card: '=',
                user: '='
            },
			link: function ($scope, $element, $attrs) {
                var goBack = function() {
                    $state.go('^');
                };

                $mdDialog.show({
                    controller: DialogController,
                    template: ['<md-dialog><md-dialog-content>',
                               '<lvg-component-card card="modalCtrl.card" board="modalCtrl.board"',
                               'project="modalCtrl.project" user="modalCtrl.user"',
                               'close="modalCtrl.close"></lvg-component-card>',
                               '</md-dialog-content></md-dialog>'].join(' '),
                    parent: angular.element(document.querySelector('#cardModalAnchor')),
                    clickOutsideToClose: true,
                    fullscreen: true,
                    locals: {
                        project: $scope.project,
                        board: $scope.board,
                        card: $scope.card,
                        user: $scope.user
                    },
                    bindToController: true,
                    controllerAs: 'modalCtrl',
                    onRemoving: goBack
                });

                function DialogController($mdDialog) {
                    this.close = function() {
                        $mdDialog.cancel();
                    };
                }

                $scope.$on('$destroy', function() {
                    $mdDialog.cancel();
                });
			}
		};
	});

})();
