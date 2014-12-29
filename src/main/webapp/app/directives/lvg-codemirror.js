(function () {

	//some parts were taken from https://github.com/angular-ui/ui-codemirror/blob/master/src/ui-codemirror.js

	'use strict';

	var directives = angular.module('lavagna.directives');

	directives.directive('lvgCodemirror', function ($timeout, $translate) {
		return {
			restrict: 'A',
			require: '?ngModel',
			link: function (scope, element, attrs, ngModel) {

				function prepare() {
					var editor = CodeMirror.fromTextArea(element[0], {
						/*lineNumbers: true, cause some ugly artifact on first load */
						lineWrapping: true,
						mode: "markdown",
						viewportMargin: Infinity
					});

					editor.on("change", function () {
						ngModel.$setViewValue(editor.getValue());
						if (!scope.$$phase) {
							scope.$apply();
						}
					});

					editor.on('keyup', function (cm, event) {
						//esc
						if (event.keyCode == 27) {
							event.preventDefault();
							event.stopPropagation();
						}
					});

					ngModel.$render = function () {
						editor.setValue(ngModel.$viewValue || '');
						//TODO: check, without timeout, the editor keep the initial empty size
						$timeout(function () {
							editor.refresh();
						});
					};
				}

				if (attrs.codemirrorPlaceholder) {
					$translate(attrs.codemirrorPlaceholder).then(function (text) {
						$(element).attr('placeholder', text);
						prepare();
					});
				} else {
					prepare();
				}
			}
		};
	});
})();