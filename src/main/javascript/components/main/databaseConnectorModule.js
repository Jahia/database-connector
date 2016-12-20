(function () {
    'use strict';

    angular.module('databaseConnector', [
        'ui.router', 'i18n', 'ngResource', 'databaseConnector.downloadFactory',
        'toaster', 'ngAnimate', 'ngSanitize', 'ngMaterial', 'ngMessages',
        'databaseConnector.dataFactory', 'ngFileUpload', 'googlechart'
    ]).config(function ($mdThemingProvider) {
        $mdThemingProvider.theme('blue-theme', 'default')
            .primaryPalette('light-blue',
                {'default': '300'});
        
    });
})();