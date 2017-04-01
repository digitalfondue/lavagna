(function () {
    'use strict';

    angular.module('lavagna.services').factory('CopyToClipboard', function ($window, $q) {
        var document = $window.document;

        var CopyToClipboard = function (text) {
            var element;
            var deferred = $q.defer();

            var handler = function () {
                return removeElement();
            };

            var removeElement = function () {
                document.body.removeEventListener('click', handler);

                document.body.removeChild(element);
                element = null;
            };

            var selectText = function () {
                element.focus();
                element.setSelectionRange(0, element.value.length);
            };

            var createElement = function () {
                document.body.addEventListener('click', handler);

                element = document.createElement('textarea');
                element.style.fontSize = '12pt';
                element.style.border = '0';
                element.style.padding = '0';
                element.style.margin = '0';
                element.style.position = 'absolute';
                element.style.left = '-9999px';

                element.style.top = ($window.pageYOffset || document.documentElement.scrollTop) + 'px';
                element.setAttribute('readonly', '');
                element.value = text;

                document.body.appendChild(element);
            };

            var copyText = function () {
                var result;

                try {
                    result = document.execCommand('copy');
                } catch (err) {
                    result = false;
                }

                if (result) {
                    deferred.resolve();
                } else {
                    deferred.reject();
                }
            };

            var cleanUp = function () {
                window.getSelection().removeAllRanges();

                removeElement();
            };

            createElement();
            selectText();
            copyText();
            cleanUp();

            return deferred.promise;
        };

        return {
            copy: function (text) {
                return new CopyToClipboard(text);
            }
        };
    });
}());
