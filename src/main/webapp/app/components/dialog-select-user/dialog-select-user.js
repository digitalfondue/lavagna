(function() {


	angular
		.module('lavagna.components')
		.component('lvgDialogSelectUser', {
			templateUrl: 'app/components/dialog-select-user/dialog-select-user.html',
			bindings: {
			    dialogTitle: '<',
				action: '&'
			},
			controller: function($mdDialog, User) {
				var ctrl = this;

				ctrl.cancel = function() {
					$mdDialog.hide();
				}

				ctrl.ok = function(user) {
					ctrl.action({user: user});
					$mdDialog.hide();
				}

				ctrl.searchUser = searchUser

				function searchUser(text) {
					return User.findUsers(text.trim()).then(function (res) {
						angular.forEach(res, function(user) {
							user.label = User.formatName(user);
						});
						return res;
					});
				};
			}
		})

})();
