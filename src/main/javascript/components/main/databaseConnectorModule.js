(function(){
    'use strict';

    angular.module('databaseConnector', [
        'ui.router', 'datatables', 'datatables.bootstrap', 'i18n',
        'ui.bootstrap', 'ui.bootstrap.tooltip', 'ngResource', 'databaseConnector.downloadFactory',
        'checklist-model', 'uiSwitch','toaster', 'ngAnimate', 'ngSanitize', 'ngMaterial', 'ngMessages',
        'databaseConnector.dataFactory', 'ngFileUpload', "googlechart"
    ]).config(function ($mdThemingProvider, $uibTooltipProvider) {
        $mdThemingProvider.theme('blue-theme', 'default')
            .primaryPalette('light-blue',
                {'default': 'A400'});
        $uibTooltipProvider.options({
            placement: 'bottom',
            popupDelay: 200,
            popupCloseDelay: 0,
            appendToBody: true
        });
    });
})();