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
                templateUrl: 'app/components/card-modal/card-modal.html',
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
