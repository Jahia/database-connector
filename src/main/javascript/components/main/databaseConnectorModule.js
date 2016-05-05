(function(){
    'use strict';
    console.log('here');
    angular.module('databaseConnector', [
        'ui.router', 'datatables', 'datatables.bootstrap', 'i18n',
        'ui.bootstrap', 'ui.bootstrap.tooltip', 'ngResource', 'databaseConnector.downloadZipFactory',
        'checklist-model', 'uiSwitch','toaster', 'ngAnimate', 'ngSanitize'
    ], function ($uibTooltipProvider) {
        $uibTooltipProvider.options({
            placement: 'bottom',
            popupDelay: 200,
            popupCloseDelay: 0,
            appendToBody: true
        });
    });
})();