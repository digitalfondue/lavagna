// move the file in a separate directory, as it will download a lot of stuff...
//
// >npm install -g typings
//
// ensure that the command typings is present in the command line
//
// >npm install angular-material-tools
//
// then run node generate-material.js
//
// copy angular-material.min.js, angular-material.layout-none.min.css in their dirs
// currently the theme does not work correctly (check why) the tab colors are not correct

'use strict';
const MaterialTools = require('angular-material-tools');

let tools = new MaterialTools({
  destination: './output',
  version: '1.1.3',
  modules: ['core', 'animate', 'layout', /*'gestures',*/ 'theming', 'palette', 'autocomplete', 'icon', /*'virtualRepeat', */
  'showHide', 'backdrop', /*'bottomSheet',*/ 'button', 'card', 'checkbox', 'chips', 'colors', 'content', 'datepicker',
  'dialog', 'divider', 'fabActions', 'fabShared', 'fabSpeedDial', /*'fabToolbar', 'gridList',*/ 'input', /*'list',*/ 'menu',
  'menuBar',/* 'navBar',*/ 'panel', 'progressCircular', 'progressLinear', 'radioButton', 'select', 'sidenav', /*'slider',
  'sticky' , 'subheader', 'swipe',*/ 'switch', 'tabs', /*'toast',*/ 'toolbar', 'tooltip', 'whiteframe'],



});

const successHandler = () => console.log('Build was successful.');
const errorHandler = error => console.error(error);

tools.build('css', 'js').then(successHandler).catch(errorHandler);         // Build all JS/CSS/themes

