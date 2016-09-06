(function () {

	'use strict';

	var components = angular.module('lavagna.components');

	components.component('lvgCardModal', {
		bindings: {
			project: '<',
			board: '<',
			card: '<',
			user: '<'
		}, 
		
		controller: ['$mdDialog', '$state', cardModalCtrl]
	});
	
	function cardModalCtrl($mdDialog, $state) {
		
		var ctrl = this;
		
		ctrl.$onInit = function init() {
			$mdDialog.show({
                controller: DialogController,
                templateUrl: 'app/components/card-modal/card-modal.html',
                parent: angular.element(document.body),
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
                onRemoving: function goBack() {
                    $state.go('^');
                }
            });
		}
		
		ctrl.$onDestroy = function() {
			$mdDialog.cancel();
		}

		function DialogController($mdDialog) {
            this.close = function() {
                $mdDialog.cancel();
            };
        }
	}	

})();
