(function(){
    'use strict';

    angular.module('databaseConnector', [
        'ui.router', 'datatables', 'datatables.bootstrap', 'i18n',
        'ui.bootstrap', 'ngResource', 'databaseConnector.downloadFactory',
        'checklist-model', 'uiSwitch','toaster', 'ngAnimate', 'ngSanitize', 'ngMaterial', 'ngMessages',
        'databaseConnector.dataFactory', 'ngFileUpload', 'googlechart'
    ]).config(function ($mdThemingProvider) {
        $mdThemingProvider.theme('blue-theme', 'default')
            .primaryPalette('light-blue',
                {'default': 'A400'});
        
    });
})();