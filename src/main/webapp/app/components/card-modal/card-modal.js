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
        templateUrl: 'app/components/card-modal/card-modal.html',
        controller: ['$document', '$state', '$window', CardModalController],
        controllerAs: 'modalCtrl'
    });

    function CardModalController($document, $state, $window) {
        var ctrl = this;
        var body = $document[0].body;
        var backDrop;
        var container;

        function close() {
            $state.go('^');
        }

        function escapeHandler($event) {
            // handle the case when the calendar is open too
            if ($event.target &&
                $event.target.parentElement &&
                $event.target.parentElement.querySelectorAll('[class^=md-calendar]').length > 0 &&
                !$window.document.body.contains($event.target)) {
                return;
            }
            //

            if ($event.keyCode === 27) {
                close();
            }
        }

        function closeHandler($event) {
            if ($event.target === container) {
                close();
            }
        }

        function cleanup() {
            container.removeEventListener('click', closeHandler);
            window.document.removeEventListener('keydown', escapeHandler);
            body.removeChild(backDrop);
            body.style.overflow = '';
        }

        ctrl.$postLink = function postLink() {
            backDrop = window.document.createElement('div');
            backDrop.className = 'lvg-card-modal__backdrop';
            body.appendChild(backDrop);
            body.style.overflow = 'hidden';

            container = window.document.querySelector('.lvg-card-modal');
            angular.element(window.document.querySelector('.lvg-card-modal__dialog')).addClass('md-transition-in');

            container.addEventListener('click', closeHandler);
            window.document.addEventListener('keydown', escapeHandler);
        };

        ctrl.$onDestroy = function () {
            cleanup();
        };

        ctrl.close = close;
    }
}());
