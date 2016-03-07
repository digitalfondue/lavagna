(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgCardModal', {
		bindings: {
			project: '=',
			board: '=',
			card: '=',
			user: '='
		}, 
		
		controller: function($mdDialog, $state, $scope) {
			
			var ctrl = this;
			
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
                parent: angular.element(angular.element(document.body)),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    project: ctrl.project,
                    board: ctrl.board,
                    card: ctrl.card,
                    user: ctrl.user
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
	});

})();
